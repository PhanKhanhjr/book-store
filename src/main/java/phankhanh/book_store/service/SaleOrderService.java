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
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.InventoryRepository;
import phankhanh.book_store.repository.OrderRepository;
import phankhanh.book_store.util.OrderMapper;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentStatus;
import phankhanh.book_store.util.constant.RefundMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SaleOrderService {
    private final OrderRepository orderRepo;
    private final InventoryRepository inventoryRepo;

    // -------------------- QUERY (SALE) --------------------
    private Specification<Order> specForSale(String q, OrderStatus status, PaymentStatus paymentStatus,
                                             Instant from, Instant to) {
        return (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("shipping").get("receiverName")), like),
                        cb.like(cb.lower(root.get("shipping").get("receiverPhone")), like),
                        cb.like(cb.lower(root.get("shipping").get("receiverEmail")), like)
                ));
            }
            if (status != null)        ps.add(cb.equal(root.get("status"), status));
            if (paymentStatus != null) ps.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            if (from != null)          ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null)            ps.add(cb.lessThan(root.get("createdAt"), to));

            cq.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(ps.toArray(Predicate[]::new));
        };
    }

    public Page<ResOrderAdmin> searchForSale(
            String q, OrderStatus status, PaymentStatus paymentStatus,
            Instant from, Instant to, Pageable pageable
    ) {
        return orderRepo.findAll(specForSale(q, status, paymentStatus, from, to), pageable)
                .map(OrderMapper.OrderAdminMapper::toAdmin);
    }

    public ResOrderAdmin getAdminView(Long id) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            OrderStatus.PENDING,    EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED),
            OrderStatus.CONFIRMED,  EnumSet.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.CANCELED),
            OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELED),
            OrderStatus.SHIPPED,    EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED,  EnumSet.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED,  EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELED,   EnumSet.noneOf(OrderStatus.class)
    );

    private boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    @Transactional
    public ResOrderAdmin updateStatusBySale(Long id, ReqUpdateOrderStatus req, Long actorId) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        OrderStatus from = o.getStatus();
        OrderStatus to   = OrderStatus.valueOf(req.status());
        if (!isAllowedTransition(from, to)) {
            throw new IllegalStateException("Invalid status transition: " + from + " -> " + to);
        }
        if (to == OrderStatus.SHIPPED) {
            if (o.getShippingFee() == null) {
                throw new IllegalStateException("Shipping info is required before marking SHIPPED");
            }
            if (o.getShippedAt() == null) {
                o.setShippedAt(Instant.now());
            }
        }
        //COMPLETED chỉ cho khi đã thanh toán
        if (to == OrderStatus.COMPLETED) {
            if (o.getPaymentStatus() != PaymentStatus.PAID) {
                throw new IllegalStateException("Order must be PAID before COMPLETED");
            }
            if (o.getCompletedAt() == null) {
                o.setCompletedAt(Instant.now());
            }
        }

        o.setStatus(to);
        o.setUpdatedAt(Instant.now());

        if (to == OrderStatus.DELIVERED) {
            if (o.getShippedAt() == null) {
                o.setShippedAt(Instant.now());
            }
            if (o.getPaymentStatus() == PaymentStatus.PAID && o.getPaidAt() == null) {
                o.setPaidAt(Instant.now());
            }
            if (from != OrderStatus.DELIVERED) {
                commitSoldOnce(o);
            }
        }

        if (to == OrderStatus.CANCELED && from != OrderStatus.CANCELED) {
            if (o.getShippedAt() == null) {
                restockItemsOnce(o);
            }
        }
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }


    private void commitSoldOnce(Order o) {
        for (OrderItem it : o.getItems()) {
            Inventory inv = inventoryRepo.findByBook_Id(it.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book " + it.getBookId()));
            inv.setSold(inv.getSold() + it.getQty());
        }
    }

    private void restockItemsOnce(Order o) {
        for (OrderItem it : o.getItems()) {
            Inventory inv = inventoryRepo.findByBook_Id(it.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book " + it.getBookId()));
            inv.setStock(inv.getStock() + it.getQty()); // cộng lại vào stock
        }
    }



    // -------------------- PAYMENT --------------------
    @Transactional
    public ResOrderAdmin updatePaymentBySale(Long id, ReqUpdatePayment req, Long actorId) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        PaymentStatus from = o.getPaymentStatus();
        PaymentStatus to   = PaymentStatus.valueOf(req.paymentStatus());

        if (to == PaymentStatus.PAID) {
            if (!(o.getStatus() == OrderStatus.CONFIRMED
                    || o.getStatus() == OrderStatus.PROCESSING
                    || o.getStatus() == OrderStatus.SHIPPED
                    || o.getStatus() == OrderStatus.DELIVERED)) {
                throw new IllegalStateException("Order must be CONFIRMED/PROCESSING/SHIPPED/DELIVERED before marked PAID");
            }
        }
        if (to == PaymentStatus.REFUND_PENDING && from != PaymentStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can move to REFUND_PENDING");
        }
        if (to == PaymentStatus.REFUNDED && from != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Must be REFUND_PENDING before REFUNDED");
        }

        // ===== Update state =====
        o.setPaymentStatus(to);
        if (to == PaymentStatus.PAID) {
            o.setPaidAt(req.paidAt() != null ? req.paidAt() : Instant.now());

            if (o.getStatus() == OrderStatus.PENDING) {
                o.setStatus(OrderStatus.CONFIRMED);
            }
        }

        //khi hoàn tiền xong: rollback sold & nhập kho
        if (to == PaymentStatus.REFUNDED) {
            rollbackSoldAndRestock(o);
        }

        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "PAYMENT_UPDATE", req.note());

        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }
    private void rollbackSoldAndRestock(Order o) {
        for (OrderItem it : o.getItems()) {
            Inventory inv = inventoryRepo.findByBook_Id(it.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book " + it.getBookId()));

            // rollback sold (không để âm)
            int newSold = Math.max(0, inv.getSold() - it.getQty());
            inv.setSold(newSold);

            // hoàn kho
            inv.setStock(inv.getStock() + it.getQty());
        }
    }



    // -------------------- SHIPPING (Sale nhập thông tin vận chuyển) --------------------
    @Transactional
    public ResOrderAdmin updateShippingBySale(Long id, ReqUpdateShipping req, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // update thông tin shipping (nếu entity có các field này)
        if (req.shippedAt() != null)       o.setShippedAt(req.shippedAt());
        if (req.fee() != null)             o.setShippingFee(req.fee());
        if (req.shippingCarrier() != null) o.setShippingCarrier(req.shippingCarrier());
        if (req.trackingCode() != null)    o.setShippingTrackingCode(req.trackingCode());

        // KHÔNG tự đổi status ở đây — chuyển trạng thái dùng API updateStatusBySale()
        o.setUpdatedAt(Instant.now());
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

    // -------------------- ASSIGN / NOTE --------------------
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
        orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // saveHistory(o, actorId, "NOTE", note);
    }

    // -------------------- CANCEL / REFUND --------------------
    @Transactional
    public ResOrderAdmin cancelBySale(Long id, String reason, Long actorId) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Chỉ cho huỷ khi chưa SHIPPED
        if (o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel after shipped");
        }
        // Nếu đã paid thì không cho huỷ trực tiếp (phải đi flow refund)
        if (o.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Paid order requires refund before cancel");
        }

        o.setStatus(OrderStatus.CANCELED);
        restockStocks(o);
        o.setUpdatedAt(Instant.now());
        // saveHistory(o, actorId, "CANCEL", reason);
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }


    // -------------------- Helpers --------------------
    private void restockStocks(Order o) {
        // hoàn tồn kho khi hủy
        for (OrderItem it : o.getItems()) {
            Inventory inv = inventoryRepo.findByBookIdForUpdate(it.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book: " + it.getBookId()));
            inv.setStock(inv.getStock() + it.getQty());
            inventoryRepo.save(inv);
        }
    }
    @Transactional
    public ResOrderAdmin refundManual(Long orderId,
                                      BigDecimal amount,
                                      RefundMethod method,
                                      Long actorId) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (o.getStatus() != OrderStatus.CANCEL_REQUESTED || o.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("Order is not eligible for manual refund");
        }

        // cập nhật trạng thái
        o.setStatus(OrderStatus.CANCELED);
        o.setPaymentStatus(PaymentStatus.REFUNDED);
        o.setRefundAmount(amount);
        o.setRefundMethod(method);
        o.setRefundedAt(Instant.now());

        orderRepo.save(o);
        return OrderMapper.OrderAdminMapper.toAdmin(o);
    }

}
