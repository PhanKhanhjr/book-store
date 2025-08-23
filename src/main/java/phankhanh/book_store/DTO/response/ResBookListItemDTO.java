package phankhanh.book_store.DTO.response;

import java.time.Instant;
public record ResBookListItemDTO(
        Long id,
        String title,
        String slug,
        String thumbnail,
        Long price,
        Long salePrice,
        Instant saleStartAt,
        Instant saleEndAt,
        Long effectivePrice,
        Integer sold
) {}
