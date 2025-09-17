package phankhanh.book_store.DTO.response;

import phankhanh.book_store.util.constant.PaymentMethod;

import java.time.Instant;
public record ResPaymentCheckout(
        String orderCode,
        PaymentMethod provider,
        String checkoutUrl,
        Instant expireAt
) {}
