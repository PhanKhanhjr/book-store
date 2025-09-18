package phankhanh.book_store.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import phankhanh.book_store.DTO.request.ReqCreateOrder;
import phankhanh.book_store.DTO.response.ResOrderCreated;
import phankhanh.book_store.DTO.response.ResOrderDetail;
import phankhanh.book_store.domain.*;
import phankhanh.book_store.util.AddressSnapshot;
import phankhanh.book_store.util.AddressSnapshotMapper;
import phankhanh.book_store.repository.*;

import phankhanh.book_store.util.OrderMapper;
import phankhanh.book_store.util.PricingUtil;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final AddressRepository addressRepo;
    private final CartRepository cartRepo;
    private final InventoryRepository inventoryRepo;
    private final BookRepository bookRepo; // bulk-load Book theo cartItem.bookId

    @Transactional
    public ResOrderCreated createOrder(Long userId, ReqCreateOrder req) {
        // 1) Lấy giỏ hàng
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found"));

        // CHỈ lấy item được chọn
        List<CartItem> items = cart.getItems().stream()
                .filter(ci -> Boolean.TRUE.equals(ci.getSelected()))
                .toList();

        if (items.isEmpty()) {
            throw new IllegalStateException("No selected items in cart");
        }

        // 2) Snapshot địa chỉ
        AddressSnapshot shipping = resolveShippingSnapshot(userId, req);

        // 3) Bulk-load Book -> Map<Long, Book>
        List<Long> bookIds = items.stream().map(CartItem::getBookId).toList();
        Map<Long, Book> bookMap = bookRepo.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        Instant now = Instant.now();

        // 4) Tính tiền (dùng PricingUtil hiện tại)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Book b = bookMap.get(ci.getBookId());
            if (b == null) throw new IllegalStateException("Book not found: id=" + ci.getBookId());
            BigDecimal unit = PricingUtil.effectivePrice(b, now);
            subtotal = subtotal.add(unit.multiply(BigDecimal.valueOf(ci.getQty())));
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = PricingUtil.discountTotal(items).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = PricingUtil.tax(subtotal.subtract(discount)).setScale(2, RoundingMode.HALF_UP);

        // pricing của m đang flat 30k, nên truyền cart hay selected items đều ra 30k
        BigDecimal shippingFee = PricingUtil.shippingFee(req.deliveryMethod(), cart).setScale(2, RoundingMode.HALF_UP);

        BigDecimal grand = subtotal.subtract(discount).add(tax).add(shippingFee)
                .setScale(2, RoundingMode.HALF_UP);

        // 5) Trừ tồn kho chỉ cho items đã chọn
        decreaseStocks(items);

        // 6) Lưu Order + OrderItems (snapshot)
        Order order = new Order();
        order.setCode(generateOrderCode());
        order.setUser(User.builder().id(userId).build()); // set id, tránh load User
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(req.paymentMethod());
        order.setPaymentStatus(
                req.paymentMethod().name().equals("COD") ? PaymentStatus.UNPAID : PaymentStatus.PENDING
        );
        order.setSubtotal(subtotal);
        order.setDiscountTotal(discount);
        order.setShippingFee(shippingFee);
        order.setTaxTotal(tax);
        order.setGrandTotal(grand);
        order.setDeliveryMethod(req.deliveryMethod());
        order.setNote(req.note());
        order.setCurrency("VND");
        order.setShipping(shipping);

        List<OrderItem> orderItems = items.stream().map(ci -> {
            Book b = bookMap.get(ci.getBookId());
            BigDecimal unit = PricingUtil.effectivePrice(b, now).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unitDiscount = PricingUtil.unitDiscount(ci).setScale(2, RoundingMode.HALF_UP);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setBookId(b.getId());
            oi.setTitleSnapshot(b.getTitle());

            // LẤY ẢNH CHÍNH AN TOÀN (không dùng getFirst)
            String mainImageUrl = mainImageUrl(b);
            if (mainImageUrl != null) {
                oi.setImageUrlSnapshot(mainImageUrl); // nếu field này tồn tại
            }

            // SKU nếu có
            if (b.getSku() != null) {
                oi.setSkuSnapshot(b.getSku()); // nếu field này tồn tại
            }

            oi.setPriceSnapshot(unit);
            oi.setDiscountSnapshot(unitDiscount);
            oi.setQty(ci.getQty());
            BigDecimal line = unit.subtract(unitDiscount)
                    .multiply(BigDecimal.valueOf(ci.getQty()))
                    .setScale(2, RoundingMode.HALF_UP);
            oi.setLineTotal(line);
            return oi;
        }).toList();

        order.setItems(orderItems);
        orderRepo.save(order);

        // 7) Chỉ xoá các item đã CHỌN khỏi giỏ, giữ lại phần chưa chọn
        cart.getItems().removeIf(ci -> Boolean.TRUE.equals(ci.getSelected()));
        cartRepo.save(cart);

        return new ResOrderCreated(order.getCode(), grand, "VND", order.getPaymentMethod(), order.getPaymentStatus());
    }

    private AddressSnapshot resolveShippingSnapshot(Long userId, ReqCreateOrder req) {
        if (req.addressId() != null) {
            Address a = addressRepo.findByIdAndUserIdActive(req.addressId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException("Address not found"));
            return AddressSnapshotMapper.fromAddress(a);
        }
        // ad-hoc: kiểm tra tối thiểu
        if (isBlank(req.receiverName()) || isBlank(req.receiverPhone())
                || isBlank(req.line1()) || isBlank(req.ward())
                || isBlank(req.district()) || isBlank(req.province())) {
            throw new IllegalArgumentException("Missing shipping fields");
        }
        return AddressSnapshotMapper.fromReq(req);
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }

    private void decreaseStocks(List<CartItem> items) {
        for (CartItem ci : items) {
            Inventory inv = inventoryRepo.findByBookIdForUpdate(ci.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book: " + ci.getBookId()));
            if (inv.getStock() < ci.getQty()) {
                throw new IllegalStateException("Out of stock");
            }
            inv.setStock(inv.getStock() - ci.getQty());
            inventoryRepo.save(inv);
        }
    }

    private String mainImageUrl(Book b) {
        try {
            // an toàn rỗng/null
            if (b.getImages() == null || b.getImages().isEmpty()) return null;
            // dùng get(0) thay cho getFirst để tương thích List cũ
            return b.getImages().get(0).getUrl();
        } catch (Exception e) {
            return null;
        }
    }

    private String generateOrderCode() {
        var d = java.time.LocalDate.now();
        String y = String.format("%02d", d.getYear() % 100);
        String m = String.format("%02d", d.getMonthValue());
        String day = String.format("%02d", d.getDayOfMonth());
        String rand = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        return "INK" + y + m + day + "-" + rand;
    }

    public ResOrderDetail getOrderDetailForUser(String code, Long userId) {
        var order = orderRepo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // Nếu chỉ cho chủ đơn xem:
        if (order.getUser() != null && !order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Forbidden");
        }
        return OrderMapper.toDetail(order);
    }

    public Page<ResOrderDetail> listMyOrders(Long userId, Pageable pageable) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(OrderMapper::toDetail);
    }
    @Transactional
    public ResOrderDetail userCancelOrder(String code, Long userId, String reason) {
        Order o = orderRepo.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!o.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not your order");
        }

        if (o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.PROCESSING) {
            if (o.getPaymentStatus() == PaymentStatus.UNPAID) {
                // hủy trực tiếp
                o.setStatus(OrderStatus.CANCELED);
                o.setCanceledAt(Instant.now());
                o.setCancelReason(reason);

                if (o.getStatus() == OrderStatus.PROCESSING) {
                    restockItemsOnce(o);
                }

            } else if (o.getPaymentStatus() == PaymentStatus.PAID) {
                o.setStatus(OrderStatus.CANCEL_REQUESTED);
                o.setCancelReason(reason);
                o.setCancelRequestedAt(Instant.now());
                o.setCancelRequestedBy(userId);
            }
        } else {
            throw new IllegalStateException("Cannot cancel order in current status");
        }

        orderRepo.save(o);
        return OrderMapper.toDetail(o);
    }

    private void restockItemsOnce(Order o) {
        for (OrderItem it : o.getItems()) {
            Inventory inv = inventoryRepo.findByBook_Id(it.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for book " + it.getBookId()));
            inv.setStock(inv.getStock() + it.getQty());
        }
    }

}
