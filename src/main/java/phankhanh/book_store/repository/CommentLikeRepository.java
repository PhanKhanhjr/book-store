package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.CommentLike;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByComment_IdAndUser_Id(Long commentId, Long userId);
    Optional<CommentLike> findByComment_IdAndUser_Id(Long commentId, Long userId);
    List<CommentLike> findByUser_IdAndComment_IdIn(Long userId, Collection<Long> commentIds);
}
