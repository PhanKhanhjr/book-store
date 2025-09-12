package phankhanh.book_store.DTO.response;

import phankhanh.book_store.util.constant.PaymentMethod;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;

public record ResOrderCreated(
        String code,
        BigDecimal grandTotal,
        String currency,              // "VND"
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {}
