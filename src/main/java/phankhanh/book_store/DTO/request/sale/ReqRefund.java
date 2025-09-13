package phankhanh.book_store.DTO.request.sale;

import java.math.BigDecimal;

public record ReqRefund(BigDecimal amount, String reason) {}