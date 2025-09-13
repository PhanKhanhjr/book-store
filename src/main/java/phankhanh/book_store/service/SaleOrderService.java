package phankhanh.book_store.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.request.sale.ReqUpdateOrderStatus;
import phankhanh.book_store.DTO.request.sale.ReqUpdatePayment;
import phankhanh.book_store.DTO.request.sale.ReqUpdateShipping;
import phankhanh.book_store.DTO.response.sale.ResOrderAdmin;
import phankhanh.book_store.domain.Inventory;
import phankhanh.book_store.domain.Order;
import phankhanh.book_store.domain.OrderItem;
import phankhanh.book_store.repository.InventoryRepository;
import phankhanh.book_store.repository.OrderRepository;
import phankhanh.book_store.util.OrderMapper;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SaleOrderService {
    private final OrderRepository orderRepo;
    private final InventoryRepository inventoryRepo;

    // ========== SALE APIs ==========
    private Specification<Order> specForSale(String q, OrderStatus status, PaymentStatus paymentStatus,
                                             Instant from, Instant to) {
        return (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                // code / tên / phone / email trong AddressSnapshot (Embeddable)
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("shipping").get("receiverName")), like),
                        cb.like(cb.lower(root.get("shipping").get("receiverPhone")), like),
                        cb.like(cb.lower(root.get("shipping").get("receiverEmail")), like)
                ));
            }
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (paymentStatus != null) ps.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            if (from != null) ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)   ps.add(cb.lessThan(root.get("createdAt"), to));

            cq.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(ps.toArray(Predicate[]::new));
        };
    }

    public Page<ResOrderAdmin> searchForSale(
            String q, OrderStatus status, PaymentStatus paymentStatus,
            Instant from, Instant to, Pageable pageable
    ) {
        var spec = specForSale(q, status, paymentStatus, from, to);
        return orderRepo.findAll(spec, pageable).map(OrderMapper.OrderAdminMapper::toAdmin);
    }

    public ResOrderAdmin getAdminView(Long id) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    @Transactional
    public ResOrderAdmin updateStatusBySale(Long id, ReqUpdateOrderStatus req, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        OrderStatus to = OrderStatus.valueOf(req.status());

        if (!isAllowedTransition(o.getStatus(), to)) {
            throw new IllegalStateException("Invalid status transition: " + o.getStatus() + " -> " + to);
        }
        // Guard: không cho ship khi chưa PACKING
        if (to == OrderStatus.SHIPPED && o.getStatus() != OrderStatus.PACKING) {
            throw new IllegalStateException("Must be PACKING before SHIPPED");
        }
        // Guard: không cho delivered khi chưa shipped
        if (to == OrderStatus.DELIVERED && o.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Must be SHIPPED before DELIVERED");
        }

        o.setStatus(to);
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "STATUS_CHANGE", req.note());
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    @Transactional
    public ResOrderAdmin updatePaymentBySale(Long id, ReqUpdatePayment req, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        PaymentStatus to = PaymentStatus.valueOf(req.paymentStatus());

        // Guard cơ bản
        if (to == PaymentStatus.PAID) {
            if (!(o.getStatus().ordinal() >= OrderStatus.CONFIRMED.ordinal())) {
                throw new IllegalStateException("Order must be CONFIRMED before marked PAID");
            }
        }
        if (to == PaymentStatus.REFUND_PENDING && o.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be moved to REFUND_PENDING");
        }
        if (to == PaymentStatus.REFUNDED && o.getPaymentStatus() != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Must be REFUND_PENDING before REFUNDED");
        }

        o.setPaymentStatus(to);
        if (req.paidAt() != null && to == PaymentStatus.PAID) {
            o.setPaidAt(req.paidAt());
        }
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "PAYMENT_UPDATE", req.note());
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    @Transactional
    public ResOrderAdmin updateShippingBySale(Long id, ReqUpdateShipping req, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Nếu entity chưa có 3 field này thì thêm vào Order:
        // private String shippingCarrier; private String shippingTrackingCode; private Instant shippedAt;
        if (req.shippedAt() != null) o.setShippedAt(req.shippedAt());
        if (req.fee() != null) o.setShippingFee(req.fee());
        if (o.getStatus() == OrderStatus.PACKING) {
            o.setStatus(OrderStatus.SHIPPED);
        }
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "SHIPPING_UPDATE", null);
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    @Transactional
    public ResOrderAdmin assignOrder(Long id, Long assigneeId, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        o.setAssigneeId(assigneeId);
        // Optional: o.setAssigneeName(...)
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "ASSIGN", "assign to " + assigneeId);
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    @Transactional
    public void addInternalNote(Long id, Long actorId, String note) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // Nếu chưa có bảng notes, tạm thời ghi vào history
        // saveHistory(o, actorId, "NOTE", note);
    }

    @Transactional
    public ResOrderAdmin cancelBySale(Long id, String reason, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!(o.getStatus() == OrderStatus.NEW || o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("Only NEW/PENDING/CONFIRMED can be cancelled");
        }
        if (o.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Paid order requires refund before cancel");
        }
        o.setStatus(OrderStatus.CANCELED);
        restockStocks(o); // hoàn tồn
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "CANCEL", reason);
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    @Transactional
    public ResOrderAdmin refundBySale(Long id, BigDecimal amount, String reason, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (o.getPaymentStatus() != PaymentStatus.PAID && o.getPaymentStatus() != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Order is not eligible for refund");
        }
        o.setPaymentStatus(PaymentStatus.REFUNDED);
        o.setRefundAmount(amount);
        o.setRefundedAt(Instant.now());
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "REFUND", reason);
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

// ========== Helpers ==========

    private boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        // Cấu hình nhanh các bước forward + hủy
        return switch (from) {
            case NEW, PENDING -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED).contains(to);
            case CONFIRMED -> EnumSet.of(OrderStatus.PACKING, OrderStatus.CANCELED).contains(to);
            case PACKING -> EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELED).contains(to);
            case SHIPPED -> EnumSet.of(OrderStatus.DELIVERED).contains(to);
            case DELIVERED, CANCELED -> false; // đã chốt
            default -> false;
        };
    }

    private void restockStocks(Order o) {
        for (OrderItem it : o.getItems()) {
            Inventory inv = inventoryRepo.findByBookIdForUpdate(it.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book: " + it.getBookId()));
            inv.setStock(inv.getStock() + it.getQty());
            inventoryRepo.save(inv);
        }
    }
}
