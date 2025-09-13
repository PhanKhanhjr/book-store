package phankhanh.book_store.DTO.response.sale;

import phankhanh.book_store.DTO.response.ResOrderItem;
import phankhanh.book_store.util.AddressSnapshot;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ResOrderAdmin(
        Long id, String code,
        String customerName, String customerPhone, String customerEmail,
        AddressSnapshot shippingAddress,
        OrderStatus status, PaymentStatus paymentStatus,
        BigDecimal subtotal, BigDecimal shippingFee, BigDecimal discount, BigDecimal tax, BigDecimal total,
        Instant createdAt, Instant updatedAt,
        String assigneeName, Long assigneeId,
        List<ResOrderItem> items
) {}
