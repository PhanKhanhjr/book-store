package phankhanh.book_store.DTO.response;

import java.time.Instant;
import java.util.List;

public record ResBookDetailDTO(
        Long id,
        String title,
        String slug,
        String sku,
        String isbn13,
        String description,

        SimpleRef publisher,
        SimpleRef supplier,
        List<SimpleRef> authors,
        List<SimpleRef> categories,

        Integer pageCount,
        Integer publicationYear,
        String language,
        Integer weightGram,
        Double widthCm,
        Double heightCm,
        Double thicknessCm,
        String coverType,
        String ageRating,

        String status,
        Long price,
        Long salePrice,
        Instant saleStartAt,
        Instant saleEndAt,
        Long effectivePrice,

        List<BookImageResponse> images,
        Integer stock,
        Integer sold,

        Instant createdAt,
        Instant updatedAt
) {
    public record SimpleRef(Long id, String name, String slug) {}
}
