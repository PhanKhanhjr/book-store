package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.util.constant.ProductStatus;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom, JpaSpecificationExecutor<Book> {

    Optional<Book> findBySlugAndDeletedFalse(String slug);

    Page<Book> findByDeletedFalse(Pageable pageable);

    Page<Book> findByStatusAndDeletedFalse(ProductStatus status, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    // filter theo category slug
    Page<Book> findByCategories_SlugAndDeletedFalse(String slug, Pageable pageable);

    // filter theo author id
    Page<Book> findByAuthors_IdAndDeletedFalse(Long authorId, Pageable pageable);

    // filter theo publisher id
    Page<Book> findByPublisher_IdAndDeletedFalse(Long publisherId, Pageable pageable);

    // filter theo supplier id
    Page<Book> findBySupplier_IdAndDeletedFalse(Long supplierId, Pageable pageable);

    // Giá “hiệu lực” (sale còn hạn thì lấy salePrice, ngược lại lấy price) trong khoảng min-max
    @Query("""
        select b from Book b
        where b.deleted = false
          and (
              (b.salePrice is not null and b.saleStartAt <= CURRENT_TIMESTAMP and (b.saleEndAt is null or b.saleEndAt >= CURRENT_TIMESTAMP) and b.salePrice between :min and :max)
              or
              ((b.salePrice is null or not (b.saleStartAt <= CURRENT_TIMESTAMP and (b.saleEndAt is null or b.saleEndAt >= CURRENT_TIMESTAMP))) and b.price between :min and :max)
          )
        """)
    Page<Book> findByEffectivePriceBetween(@Param("min") long min,
                                           @Param("max") long max,
                                           Pageable pageable);
}
