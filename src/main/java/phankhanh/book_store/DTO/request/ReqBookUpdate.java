package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ReqBookUpdate(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String slug,
        @Size(max = 32)  String sku,
        @Size(max = 20)  String isbn13,
        String description,

        @NotNull Long publisherId,
        @NotNull Long supplierId,
        @NotEmpty Set<Long> authorIds,
        @NotEmpty Set<Long> categoryIds,

        @Positive @Max(100000) Integer pageCount,
        @Min(1000) @Max(2100)  Integer publicationYear,
        @NotBlank String language,
        @PositiveOrZero Integer weightGram,
        @PositiveOrZero Double widthCm,
        @PositiveOrZero Double heightCm,
        @PositiveOrZero Double thicknessCm,
        @NotBlank String coverType,
        @NotBlank String ageRating,

        @NotBlank String status,
        @NotNull @Positive Long price,
        @Positive Long salePrice,
        Instant saleStartAt,
        Instant saleEndAt,

        @NotEmpty List<ImageItem> images
) {
    public record ImageItem(
            @NotBlank @Size(max = 500) String url,
            @NotNull @PositiveOrZero Integer sortOrder
    ) {}
}
