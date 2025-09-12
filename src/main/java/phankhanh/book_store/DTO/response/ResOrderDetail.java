package phankhanh.book_store.DTO.response;

import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ResOrderDetail(
        String code,
        OrderStatus status,
        PaymentStatus paymentStatus,

        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal shippingFee,
        BigDecimal taxTotal,
        BigDecimal grandTotal,

        // shipping snapshot
        String receiverName,
        String receiverPhone,
        String receiverEmail,
        String addressLine,
        String wardCode,
        String districtCode,
        String provinceCode,
        String postalCode,

        // timeline
        Instant createdAt,
        Instant confirmedAt,
        Instant shippedAt,
        Instant completedAt,
        Instant canceledAt,

        List<ResOrderItem> items
) {}
