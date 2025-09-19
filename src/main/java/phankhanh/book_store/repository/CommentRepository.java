package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Comment;
import phankhanh.book_store.util.constant.CommentStatus;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByBook_IdAndParentIsNullAndDeletedAtIsNullAndStatus(Long bookId, CommentStatus status, Pageable pageable);
    List<Comment> findByParent_IdInAndDeletedAtIsNullAndStatus(Collection<Long> parentIds, CommentStatus status);
    long countByBook_IdAndDeletedAtIsNullAndStatus(Long bookId,CommentStatus status);
    Page<Comment> findByBook_IdAndParentIsNullAndDeletedAtIsNull(Long bookId, Pageable pageable);

    List<Comment> findByParent_IdInAndDeletedAtIsNull(Collection<Long> parentIds);

    long countByBook_IdAndDeletedAtIsNull(Long bookId);
}
