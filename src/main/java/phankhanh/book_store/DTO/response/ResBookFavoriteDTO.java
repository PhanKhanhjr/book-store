package phankhanh.book_store.DTO.response;

import java.math.BigDecimal;

public record ResBookFavoriteDTO(
        Long id,
        String title,
        String slug,
        String coverUrl,
        BigDecimal price,
        BigDecimal effectivePrice,
        boolean likedByMe,
        long favoriteCount
) {}


