package phankhanh.book_store.DTO.request;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ReqBookCreate(

        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String slug,          // optional: nếu null, service có thể auto-gen từ title
        @Size(max = 32)  String sku,
        @Size(max = 20)  String isbn13,
        String description,

        @NotNull Long publisherId,
        @NotNull Long supplierId,
        @NotEmpty Set<Long> authorIds,
        @NotEmpty Set<Long> categoryIds,

        // Thuộc tính vật lý
        @Positive @Max(100000) Integer pageCount,
        @Min(1000) @Max(2100)  Integer publicationYear,
        @NotBlank String language,
        @PositiveOrZero Integer weightGram,
        @PositiveOrZero Double widthCm,
        @PositiveOrZero Double heightCm,
        @PositiveOrZero Double thicknessCm,
        @NotBlank String coverType,
        @NotBlank String ageRating,
        // Thương mại
        @NotBlank String status,
        @NotNull @Positive BigDecimal price,
        @Positive BigDecimal salePrice,
        Instant saleStartAt,
        Instant saleEndAt,

        // Ảnh & tồn kho
        List<ImageItem> images,
        @NotNull @PositiveOrZero Integer initialStock
) {
    public record ImageItem(
            @NotBlank @Size(max = 500) String url,
            @NotNull @PositiveOrZero Integer sortOrder
    ) {}
}
