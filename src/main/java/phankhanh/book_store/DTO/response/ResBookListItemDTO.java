package phankhanh.book_store.DTO.response;

import lombok.Builder;
import phankhanh.book_store.util.constant.ProductStatus;

import java.time.Instant;
@Builder
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
        Integer sold,
        ProductStatus status
) {}
