package phankhanh.book_store.DTO.response;

import java.math.BigDecimal;

public record ResOrderItem(
        Long bookId,
        String title,
        String imageUrl,
        String sku,
        BigDecimal price,
        BigDecimal discount,
        Integer qty,
        BigDecimal lineTotal
) {}
