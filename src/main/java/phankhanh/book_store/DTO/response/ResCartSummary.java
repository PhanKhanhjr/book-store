package phankhanh.book_store.DTO.response;

import java.math.BigDecimal;
import java.util.List;

public record ResCartSummary(
        List<ResCartItem> items,
        BigDecimal subtotal, BigDecimal discountTotal, BigDecimal shippingFee,
        BigDecimal taxTotal, BigDecimal grandTotal,
        Integer totalItems, Integer totalSelected
) {}
