package phankhanh.book_store.DTO.request.sale;

import java.math.BigDecimal;
import java.time.Instant;

public record ReqUpdateShipping(String carrier, String trackingCode, Instant shippedAt, BigDecimal fee) {}
