package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.BookImage;

import java.util.List;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBook_IdOrderBySortOrderAsc(Long bookId);
    long deleteByBook_Id(Long bookId);
}
