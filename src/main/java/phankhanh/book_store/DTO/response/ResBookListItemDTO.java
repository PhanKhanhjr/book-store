package phankhanh.book_store.DTO.response;

import lombok.Builder;
import phankhanh.book_store.util.constant.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
@Builder
public record ResBookListItemDTO(
        Long id,
        String title,
        String slug,
        String thumbnail,
        BigDecimal price,
        BigDecimal salePrice,
        Instant saleStartAt,
        Instant saleEndAt,
        BigDecimal effectivePrice,
        Integer sold,
        ProductStatus status
) {}
