package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Rating;

import java.util.Optional;


public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByBook_IdAndUser_IdAndDeletedAtIsNull(Long bookId, Long userId);
    Optional<Rating> findByIdAndDeletedAtIsNull(Long id);

    Page<Rating> findByBook_IdAndDeletedAtIsNull(Long bookId, Pageable pageable);

    Page<Rating> findByBook_IdAndDeletedAtIsNullAndContentIsNotNullAndContentNot(Long bookId, String empty, Pageable pageable);

    long countByBook_IdAndDeletedAtIsNull(Long bookId);
    long countByBook_IdAndScoreAndDeletedAtIsNull(Long bookId, Integer score);
}
