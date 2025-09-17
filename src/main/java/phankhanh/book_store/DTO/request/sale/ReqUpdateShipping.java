package phankhanh.book_store.DTO.request.sale;

import java.math.BigDecimal;
import java.time.Instant;

public record ReqUpdateShipping(
        BigDecimal fee,
        String shippingCarrier,
        String trackingCode,
        Instant shippedAt
) {}
