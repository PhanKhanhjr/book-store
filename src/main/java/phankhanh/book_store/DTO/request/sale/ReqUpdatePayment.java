package phankhanh.book_store.DTO.request.sale;

import java.time.Instant;

public record ReqUpdatePayment(String paymentStatus, Instant paidAt, String transactionId, String note) {}
