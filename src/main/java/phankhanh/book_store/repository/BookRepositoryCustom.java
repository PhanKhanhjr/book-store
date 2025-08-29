
package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.util.constant.ProductStatus;

public interface BookRepositoryCustom {
    Page<Book> searchBooks(
            String q,
            Long authorId,
            Long categoryId,
            Long publisherId,
            Long supplierId,
            ProductStatus status,
            Pageable pageable
    );
    java.util.List<Book> findNewest(ProductStatus status, int limit);
    java.util.List<Book> findBestSellers(ProductStatus status, int limit);
    java.util.List<Book> findOnSale(ProductStatus status, int limit, java.time.Instant now);
    java.util.List<Book> findTopByCategory(Long categoryId, ProductStatus status, int limit);
}
