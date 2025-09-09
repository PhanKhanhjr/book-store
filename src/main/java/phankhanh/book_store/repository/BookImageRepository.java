package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.BookImage;

import java.util.List;
import java.util.Optional;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    @Query("select coalesce(max(bi.sortOrder), 0) from BookImage bi where bi.book.id = :bookId")
    Integer findMaxSortOrderByBookId(@Param("bookId") Long bookId);

    Optional<BookImage> findFirstByBookIdAndSortOrder(Long bookId, Integer sortOrder);
}
