package phankhanh.book_store.util;

import phankhanh.book_store.DTO.request.*;
import phankhanh.book_store.DTO.response.*;
import phankhanh.book_store.domain.*;
import phankhanh.book_store.util.constant.*;

import java.math.BigDecimal;
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

    // Catalog/List item: trả thumbnail (160x240.webp) của ảnh chính (sortOrder=0), nếu không có thì ảnh đầu tiên.
    public static ResBookListItemDTO toListItem(Book b) {
        BookImage main = chooseMainImage(b.getImages());
        String thumb = (main != null) ? toThumbUrl(main.getUrl()) : null;

        BigDecimal effective = calcEffectivePrice(b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt());
        int sold = b.getInventory() != null ? b.getInventory().getSold() : 0;

        return new ResBookListItemDTO(
                b.getId(), b.getTitle(), b.getSlug(), thumb,
                b.getPrice(), b.getSalePrice(),
                b.getSaleStartAt(), b.getSaleEndAt(), effective, sold, b.getStatus()
        );
    }

    // Detail: trả full list ảnh (kèm variants)
    public static ResBookDetailDTO toDetail(Book b) {
        BigDecimal effective = calcEffectivePrice(b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt());

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

        // ➜ SỬA Ở ĐÂY: map sang BookImageResponse (có variants)
        var images = b.getImages() != null ? b.getImages().stream()
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(BookMapper::toImageResponse)
                .toList() : List.<BookImageResponse>of();

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

    private static BigDecimal calcEffectivePrice(
            BigDecimal price,
            BigDecimal salePrice,
            Instant start,
            Instant end
    ) {
        BigDecimal base = (price != null) ? price : BigDecimal.ZERO;

        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0) {
            Instant now = Instant.now();
            boolean inWindow =
                    (start == null || !now.isBefore(start)) &&
                            (end == null   || !now.isAfter(end));

            if (inWindow) {
                // Optional: nếu không muốn giá sale cao hơn giá gốc
                if (base.signum() > 0 && salePrice.compareTo(base) > 0) {
                    return base;
                }
                return salePrice;
            }
        }
        return base;
    }


    // Ưu tiên ảnh sortOrder = 0, nếu không có thì lấy ảnh có sortOrder nhỏ nhất
    private static BookImage chooseMainImage(List<BookImage> images) {
        if (images == null || images.isEmpty()) return null;
        for (BookImage bi : images) {
            if (bi.getSortOrder() != null && bi.getSortOrder() == 0) return bi;
        }
        return images.stream()
                .min(Comparator.comparingInt(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .orElse(null);
    }

    // URL thumbnail 160x240 (Firebase Extension pattern: name_WIDTHxHEIGHT.webp)
    private static String toThumbUrl(String originalUrl) {
        return withSize(originalUrl, 320, 480);
    }

    // Map 1 BookImage -> BookImageResponse (có variants)
    public static BookImageResponse toImageResponse(BookImage i) {
        String u = i.getUrl();
        return BookImageResponse.builder()
                .id(i.getId())
                .url(u)
                .sortOrder(i.getSortOrder())
                .variants(BookImageResponse.Variants.builder()
                        .thumb(withSize(u, 160, 240))
                        .medium(withSize(u, 320, 480))
                        .large(withSize(u, 640, 960))
                        .xlarge(withSize(u, 960, 1440))
                        .build())
                .build();
    }

    /** Tạo URL biến thể theo pattern của Firebase Resize Images:
     *  abc.jpg -> abc_160x240.webp (nếu outExt=null thì giữ ext gốc) */
    private static String withSize(String url, int w, int h) {
        int q = url.indexOf('?');
        String base = (q >= 0) ? url.substring(0, q) : url;
        int dot = base.lastIndexOf('.');
        if (dot < 0) return base; // không có extension
        String name = base.substring(0, dot);
        String ext  = base.substring(dot + 1); // lấy đuôi gốc (.jpg, .png, .webp)
        return name + "_" + w + "x" + h + "." + ext;
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
