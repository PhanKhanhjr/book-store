package phankhanh.book_store.util;

import phankhanh.book_store.DTO.request.*;
import phankhanh.book_store.DTO.response.*;
import phankhanh.book_store.domain.*;
import phankhanh.book_store.util.constant.AgeRating;
import phankhanh.book_store.util.constant.CoverType;
import phankhanh.book_store.util.constant.Language;
import phankhanh.book_store.util.constant.ProductStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BookMapper {

    /* ========== REQ -> ENTITY ========== */
    public static void mapCreateReqToEntity(ReqBookCreate req,
                                            Book b,
                                            Publisher publisher,
                                            Supplier supplier,
                                            Set<Author> authors,
                                            Set<Category> categories) {
        b.setTitle(req.title());
        b.setSlug(req.slug());
        b.setSku(req.sku());
        b.setIsbn13(req.isbn13());
        b.setDescription(req.description());

        b.setPublisher(publisher);
        b.setSupplier(supplier);
        b.setAuthors(authors);
        b.setCategories(categories);

        b.setPageCount(req.pageCount());
        b.setPublicationYear(req.publicationYear());
        b.setLanguage(Language.valueOf(req.language()));
        b.setWeightGram(req.weightGram());
        b.setWidthCm(req.widthCm());
        b.setHeightCm(req.heightCm());
        b.setThicknessCm(req.thicknessCm());
        b.setCoverType(CoverType.valueOf(req.coverType()));
        b.setAgeRating(AgeRating.valueOf(req.ageRating()));
        b.setStatus(ProductStatus.valueOf(req.status()));
        b.setPrice(req.price());
        b.setSalePrice(req.salePrice());
        b.setSaleStartAt(req.saleStartAt());
        b.setSaleEndAt(req.saleEndAt());

        b.setImages(req.images().stream()
                .map(i -> BookImage.builder()
                        .url(i.url())
                        .sortOrder(i.sortOrder())
                        .book(b)
                        .build())
                .collect(Collectors.toList()));
    }

    public static void mapUpdateReqToEntity(ReqBookUpdate req,
                                            Book b,
                                            Publisher publisher,
                                            Supplier supplier,
                                            Set<Author> authors,
                                            Set<Category> categories) {
        b.setTitle(req.title());
        b.setSlug(req.slug());
        b.setSku(req.sku());
        b.setIsbn13(req.isbn13());
        b.setDescription(req.description());

        b.setPublisher(publisher);
        b.setSupplier(supplier);
        b.setAuthors(authors);
        b.setCategories(categories);

        b.setPageCount(req.pageCount());
        b.setPublicationYear(req.publicationYear());
        b.setLanguage(Language.valueOf(req.language()));
        b.setWeightGram(req.weightGram());
        b.setWidthCm(req.widthCm());
        b.setHeightCm(req.heightCm());
        b.setThicknessCm(req.thicknessCm());
        b.setCoverType(CoverType.valueOf(req.coverType()));
        b.setAgeRating(AgeRating.valueOf(req.ageRating()));
        b.setStatus(ProductStatus.valueOf(req.status()));
        b.setPrice(req.price());
        b.setSalePrice(req.salePrice());
        b.setSaleStartAt(req.saleStartAt());
        b.setSaleEndAt(req.saleEndAt());

        b.setImages(req.images().stream()
                .map(i -> BookImage.builder()
                        .url(i.url())
                        .sortOrder(i.sortOrder())
                        .book(b)
                        .build())
                .collect(Collectors.toList()));
    }

    /* ========== ENTITY -> DTO ========== */

    public static ResBookListItemDTO toListItem(Book b) {
        String thumb = b.getImages().stream()
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(BookImage::getUrl).findFirst().orElse(null);

        Long effective = calcEffectivePrice(b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt());
        int sold = b.getInventory() != null ? b.getInventory().getSold() : 0;

        return new ResBookListItemDTO(
                b.getId(), b.getTitle(), b.getSlug(), thumb,
                b.getPrice(), b.getSalePrice(),
                b.getSaleStartAt(), b.getSaleEndAt(), effective, sold
        );
    }

    public static ResBookDetailDTO toDetail(Book b) {
        Long effective = calcEffectivePrice(b.getPrice(), b.getSalePrice(), b.getSaleStartAt(), b.getSaleEndAt());

        var publisher = new ResBookDetailDTO.SimpleRef(b.getPublisher().getId(), b.getPublisher().getName(), null);
        var supplier = new ResBookDetailDTO.SimpleRef(b.getSupplier().getId(), b.getSupplier().getName(), null);

        var authors = b.getAuthors().stream()
                .map(a -> new ResBookDetailDTO.SimpleRef(a.getId(), a.getName(), null))
                .toList();

        var categories = b.getCategories().stream()
                .map(c -> new ResBookDetailDTO.SimpleRef(c.getId(), c.getName(), c.getSlug()))
                .toList();

        var images = b.getImages().stream()
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() == null ? Integer.MAX_VALUE : i.getSortOrder()))
                .map(i -> new ResBookDetailDTO.ImageItem(i.getId(), i.getUrl(), i.getSortOrder()))
                .toList();

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

    /* ========== Helper ========== */
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
}
