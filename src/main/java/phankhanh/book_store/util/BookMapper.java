package phankhanh.book_store.util;

import phankhanh.book_store.DTO.request.*;
import phankhanh.book_store.DTO.response.*;
import phankhanh.book_store.domain.*;
import phankhanh.book_store.util.constant.*;

import java.time.Instant;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class BookMapper {
    private BookMapper() {}

    /* ================= REQ -> ENTITY ================= */

    // Tạo entity mới từ req (chỉ map field primitive/enums + images). Quan hệ sẽ gắn ở Service.
    public static Book toEntity(ReqBookCreate req) {
        Book b = Book.builder()
                .title(req.title())
                .slug(req.slug())
                .sku(req.sku())
                .isbn13(req.isbn13())
                .description(req.description())
                .pageCount(req.pageCount())
                .publicationYear(req.publicationYear())
                .language(parseEnum(Language.class, req.language()))
                .weightGram(req.weightGram())
                .widthCm(req.widthCm())
                .heightCm(req.heightCm())
                .thicknessCm(req.thicknessCm())
                .coverType(parseEnum(CoverType.class, req.coverType()))
                .ageRating(parseEnum(AgeRating.class, req.ageRating()))
                .status(parseEnum(ProductStatus.class, req.status()))
                .price(req.price())
                .salePrice(req.salePrice())
                .saleStartAt(req.saleStartAt())
                .saleEndAt(req.saleEndAt())
                .build();

        if (req.images() != null) {
            List<BookImage> images = req.images().stream()
                    .map(i -> BookImage.builder()
                            .url(i.url())
                            .sortOrder(i.sortOrder())
                            .book(b) // backref
                            .build())
                    .collect(Collectors.toList());
            b.setImages(images);
        }
        return b;
    }

    // Update entity hiện có từ req (chỉ map field; quan hệ gắn ở Service)
    public static void updateEntity(Book b, ReqBookUpdate req) {
        if (req.title() != null) b.setTitle(req.title());
        if (req.slug() != null) b.setSlug(req.slug());
        if (req.sku() != null) b.setSku(req.sku());
        if (req.isbn13() != null) b.setIsbn13(req.isbn13());
        if (req.description() != null) b.setDescription(req.description());

        if (req.pageCount() != null) b.setPageCount(req.pageCount());
        if (req.publicationYear() != null) b.setPublicationYear(req.publicationYear());
        if (req.language() != null) b.setLanguage(parseEnum(Language.class, req.language()));
        if (req.weightGram() != null) b.setWeightGram(req.weightGram());
        if (req.widthCm() != null) b.setWidthCm(req.widthCm());
        if (req.heightCm() != null) b.setHeightCm(req.heightCm());
        if (req.thicknessCm() != null) b.setThicknessCm(req.thicknessCm());
        if (req.coverType() != null) b.setCoverType(parseEnum(CoverType.class, req.coverType()));
        if (req.ageRating() != null) b.setAgeRating(parseEnum(AgeRating.class, req.ageRating()));
        if (req.status() != null) b.setStatus(parseEnum(ProductStatus.class, req.status()));
        if (req.price() != null) b.setPrice(req.price());
        if (req.salePrice() != null) b.setSalePrice(req.salePrice());
        if (req.saleStartAt() != null) b.setSaleStartAt(req.saleStartAt());
        if (req.saleEndAt() != null) b.setSaleEndAt(req.saleEndAt());

        // Replace images (đơn giản, đúng orphanRemoval). Nếu muốn merge thông minh, xử lý theo id ảnh.
        if (req.images() != null) {
            b.getImages().clear();
            for (var i : req.images()) {
                b.getImages().add(BookImage.builder()
                        .url(i.url())
                        .sortOrder(i.sortOrder())
                        .book(b)
                        .build());
            }
        }
    }

    /* ================= ENTITY -> DTO ================= */

    public static ResBookListItemDTO toListItem(Book b) {
        String thumb = b.getImages().stream()
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(BookImage::getUrl).findFirst().orElse(null);

        Long effective = calcEffectivePrice(b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt());
        int sold = b.getInventory() != null ? b.getInventory().getSold() : 0;

        return new ResBookListItemDTO(
                b.getId(), b.getTitle(), b.getSlug(), thumb,
                b.getPrice(), b.getSalePrice(),
                b.getSaleStartAt(), b.getSaleEndAt(), effective, sold, b.getStatus()
        );
    }

    public static ResBookDetailDTO toDetail(Book b) {
        Long effective = calcEffectivePrice(b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt());

        var publisher = (b.getPublisher() != null)
                ? new ResBookDetailDTO.SimpleRef(b.getPublisher().getId(), b.getPublisher().getName(), null) : null;
        var supplier = (b.getSupplier() != null)
                ? new ResBookDetailDTO.SimpleRef(b.getSupplier().getId(), b.getSupplier().getName(), null) : null;

        var authors = b.getAuthors() != null ? b.getAuthors().stream()
                .map(a -> new ResBookDetailDTO.SimpleRef(a.getId(), a.getName(), null))
                .toList() : List.<ResBookDetailDTO.SimpleRef>of();

        var categories = b.getCategories() != null ? b.getCategories().stream()
                .map(c -> new ResBookDetailDTO.SimpleRef(c.getId(), c.getName(), c.getSlug()))
                .toList() : List.<ResBookDetailDTO.SimpleRef>of();

        var images = b.getImages() != null ? b.getImages().stream()
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(i -> new ResBookDetailDTO.ImageItem(i.getId(), i.getUrl(), i.getSortOrder()))
                .toList() : List.<ResBookDetailDTO.ImageItem>of();

        Integer stock = b.getInventory() != null ? b.getInventory().getStock() : 0;
        Integer sold  = b.getInventory() != null ? b.getInventory().getSold()  : 0;

        return new ResBookDetailDTO(
                b.getId(), b.getTitle(), b.getSlug(), b.getSku(), b.getIsbn13(), b.getDescription(),
                publisher, supplier, authors, categories,
                b.getPageCount(), b.getPublicationYear(),
                b.getLanguage() != null ? b.getLanguage().name() : null,
                b.getWeightGram(), b.getWidthCm(), b.getHeightCm(), b.getThicknessCm(),
                b.getCoverType() != null ? b.getCoverType().name() : null,
                b.getAgeRating() != null ? b.getAgeRating().name() : null,
                b.getStatus() != null ? b.getStatus().name() : null,
                b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt(),
                effective, images, stock, sold, b.getCreatedAt(), b.getUpdatedAt()
        );
    }

    /* ================= Helper ================= */

    private static long calcEffectivePrice(Long price, Long salePrice, Instant start, Instant end) {
        long p = price == null ? 0 : price;
        if (salePrice != null) {
            var now = Instant.now();
            boolean inWin = (start == null || !now.isBefore(start)) &&
                    (end == null || !now.isAfter(end));
            if (inWin) return salePrice;
        }
        return p;
    }

    // Parse enum an toàn, không phân biệt hoa thường; trả null nếu null; quăng lỗi rõ nếu sai giá trị
    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name) {
        if (name == null) return null;
        try {
            return Enum.valueOf(type, name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid enum value for " + type.getSimpleName() + ": " + name);
        }
    }
}
