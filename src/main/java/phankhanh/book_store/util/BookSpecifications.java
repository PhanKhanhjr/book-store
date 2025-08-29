package phankhanh.book_store.util;

import org.springframework.data.jpa.domain.Specification;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.util.constant.AgeRating;
import phankhanh.book_store.util.constant.Language;
import phankhanh.book_store.util.constant.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public class BookSpecifications {

    public static Specification<Book> hasCategory(String categorySlug) {
        return (root, query, cb) -> categorySlug == null ? null :
                cb.equal(root.join("categories").get("slug"), categorySlug);
    }

    public static Specification<Book> hasPublisher(String publisherSlug) {
        return (root, query, cb) -> publisherSlug == null ? null :
                cb.equal(root.join("publisher").get("slug"), publisherSlug);
    }

    public static Specification<Book> hasSupplier(String supplierSlug) {
        return (root, query, cb) -> supplierSlug == null ? null :
                cb.equal(root.join("supplier").get("slug"), supplierSlug);
    }

    public static Specification<Book> hasLanguage(Language language) {
        return (root, query, cb) -> language == null ? null :
                cb.equal(root.get("language"), language);
    }

    public static Specification<Book> hasStatus(ProductStatus status) {
        return (root, query, cb) -> status == null ? null :
                cb.equal(root.get("status"), status);
    }

    public static Specification<Book> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Book> priceGte(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Book> priceLte(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Book> priceBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        if (min == null)  return priceLte(max);
        if (max == null)  return priceGte(min);
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }

    public static Specification<Book> ageGte(Integer minAge) {
        return (root, query, cb) -> minAge == null ? null : cb.greaterThanOrEqualTo(root.get("ageRating"), minAge);
    }

    public static Specification<Book> ageLte(Integer maxAge) {
        return (root, query, cb) -> maxAge == null ? null : cb.lessThanOrEqualTo(root.get("ageRating"), maxAge);
    }

    public static Specification<Book> ageBetween(Integer minAge, Integer maxAge) {
        if (minAge == null && maxAge == null) return null;
        if (minAge == null) return ageLte(maxAge);
        if (maxAge == null) return ageGte(minAge);
        return (root, query, cb) -> cb.between(root.get("ageRating"), minAge, maxAge);
    }
    public static Specification<Book> ageInRange(AgeRating min, AgeRating max) {
        if (min == null && max == null) return null;
        List<AgeRating> allowed = AgeRatingUtil.between(min, max);
        return (root, query, cb) -> root.get("ageRating").in(allowed);
    }
}

