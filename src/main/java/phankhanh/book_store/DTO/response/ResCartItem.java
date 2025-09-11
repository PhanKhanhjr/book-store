package phankhanh.book_store.DTO.response;

import java.math.BigDecimal;

public record ResCartItem(
        Long bookId, String title, String slug,
        Integer qty, Boolean selected,
        BigDecimal unitPrice, BigDecimal lineTotal,
        Integer stockAvailable
) {}
