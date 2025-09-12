package phankhanh.book_store.DTO.response;

import java.math.BigDecimal;

public record ResCartItem(
        Long bookId, String title, String slug,
        String thumbnail,
        Boolean onSale,
        Integer qty, Boolean selected,
        BigDecimal originalUnitPrice,
        BigDecimal unitPrice, BigDecimal lineTotal,
        Integer stockAvailable
) {}
