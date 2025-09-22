package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.request.ReqCreateComment;
import phankhanh.book_store.DTO.request.ReqUpdateComment;
import phankhanh.book_store.DTO.response.ResComment;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.Comment;
import phankhanh.book_store.domain.CommentLike;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.CommentLikeRepository;
import phankhanh.book_store.repository.CommentRepository;
import phankhanh.book_store.repository.UserRepository;
import phankhanh.book_store.util.CommentMapper;
import phankhanh.book_store.util.constant.CommentStatus;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final CommentLikeRepository likeRepo;

    @Transactional
    public ResComment create(Long bookId, Long userId, ReqCreateComment req) {
        Book book = bookRepo.getReferenceById(bookId);
        User user = userRepo.getReferenceById(userId);

        Comment parent = null;
        if (req.parentId() != null) {
            parent = commentRepo.findById(req.parentId())
                    .orElseThrow(() -> new NoSuchElementException("Parent comment not found"));
            if (!Objects.equals(parent.getBook().getId(), bookId)) {
                throw new IllegalArgumentException("Parent comment not in this book");
            }
        }

        Comment c = Comment.builder()
                .book(book)
                .user(user)
                .parent(parent)
                .content(req.content().trim())
                .status(CommentStatus.ACTIVE)
                .createdAt(Instant.now())
                .likeCount(0)
                .build();

        c = commentRepo.save(c);

        String name = Optional.ofNullable(user.getUsername()).orElse(user.getEmail());
        return CommentMapper.toDto(c, List.of(), name, null, c.getLikeCount(), false);
    }

    @Transactional
    public ResComment update(Long commentId, Long userId, boolean isAdmin, ReqUpdateComment req) {
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        if (c.getDeletedAt() != null) {
            throw new IllegalStateException("Comment already deleted");
        }
        if (!isAdmin && !Objects.equals(c.getUser().getId(), userId)) {
            throw new SecurityException("Not allowed");
        }

        c.setContent(req.content().trim());
        c.setUpdatedAt(Instant.now());

        String name = Optional.ofNullable(c.getUser().getFullName()).orElse(c.getUser().getEmail());
        return CommentMapper.toDto(c, List.of(), name, null, c.getLikeCount(), null);
    }

    @Transactional
    public void delete(Long commentId, Long userId, boolean isAdmin) {
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        if (c.getDeletedAt() != null) return;
        if (!isAdmin && !Objects.equals(c.getUser().getId(), userId)) {
            throw new SecurityException("Not allowed");
        }

        c.setDeletedAt(Instant.now());
        c.setDeletedBy(userId);
    }

    @Transactional
    public void hideOrUnhide(Long commentId, boolean hide) {
        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        if (c.getDeletedAt() != null) throw new IllegalStateException("Comment deleted");

        c.setStatus(hide ? CommentStatus.HIDDEN : CommentStatus.ACTIVE);
        c.setUpdatedAt(Instant.now());
    }

    @Transactional
    public Map<String, Object> listByBook(Long bookId, Pageable pageable,
                                          boolean includeHidden, boolean isAdmin,
                                          Long currentUserIdOrNull) {
        final boolean showHidden = includeHidden && isAdmin;

        Page<Comment> roots;
        List<Comment> children;
        long total;

        if (showHidden) {
            roots = commentRepo.findByBook_IdAndParentIsNullAndDeletedAtIsNull(bookId, pageable);
            List<Long> rootIds = roots.stream().map(Comment::getId).toList();
            children = rootIds.isEmpty() ? List.of()
                    : commentRepo.findByParent_IdInAndDeletedAtIsNull(rootIds);
            total = commentRepo.countByBook_IdAndDeletedAtIsNull(bookId);
        } else {
            roots = commentRepo.findByBook_IdAndParentIsNullAndDeletedAtIsNullAndStatus(
                    bookId, CommentStatus.ACTIVE, pageable);
            List<Long> rootIds = roots.stream().map(Comment::getId).toList();
            children = rootIds.isEmpty() ? List.of()
                    : commentRepo.findByParent_IdInAndDeletedAtIsNullAndStatus(rootIds, CommentStatus.ACTIVE);
            total = commentRepo.countByBook_IdAndDeletedAtIsNullAndStatus(bookId, CommentStatus.ACTIVE);
        }

        // check like
        List<Long> allIds = new ArrayList<>();
        allIds.addAll(roots.stream().map(Comment::getId).toList());
        allIds.addAll(children.stream().map(Comment::getId).toList());

        Set<Long> likedSet = Set.of();
        if (currentUserIdOrNull != null && !allIds.isEmpty()) {
            likedSet = likeRepo.findByUser_IdAndComment_IdIn(currentUserIdOrNull, allIds)
                    .stream().map(l -> l.getComment().getId())
                    .collect(Collectors.toSet());
        }

        Map<Long, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        Set<Long> finalLikedSet = likedSet;
        List<ResComment> rootDtos = roots.stream().map(r -> {
            List<ResComment> childDtos = childrenMap.getOrDefault(r.getId(), List.of())
                    .stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .map(ch -> CommentMapper.toDto(
                            ch,
                            List.of(),
                            Optional.ofNullable(ch.getUser().getFullName()).orElse(ch.getUser().getEmail()),
                            null,
                            ch.getLikeCount(),
                            currentUserIdOrNull == null ? null : finalLikedSet.contains(ch.getId())
                    ))
                    .toList();

            return CommentMapper.toDto(
                    r,
                    childDtos,
                    Optional.ofNullable(r.getUser().getFullName()).orElse(r.getUser().getEmail()),
                    null,
                    r.getLikeCount(),
                    currentUserIdOrNull == null ? null : finalLikedSet.contains(r.getId())
            );
        }).toList();

        Map<String, Object> res = new HashMap<>();
        res.put("content", rootDtos);
        res.put("page", roots.getNumber());
        res.put("size", roots.getSize());
        res.put("totalPages", roots.getTotalPages());
        res.put("totalComments", total);
        return res;
    }

    @Transactional
    public void like(Long commentId, Long userId) {
        if (likeRepo.existsByComment_IdAndUser_Id(commentId, userId)) return;

        Comment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        if (c.getDeletedAt() != null || c.getStatus() == CommentStatus.HIDDEN) {
            throw new IllegalStateException("Comment not available");
        }

        User u = userRepo.getReferenceById(userId);
        CommentLike like = CommentLike.builder()
                .comment(c)
                .user(u)
                .createdAt(Instant.now())
                .build();
        likeRepo.save(like);

        c.setLikeCount((c.getLikeCount() == null ? 0 : c.getLikeCount()) + 1);
    }

    @Transactional
    public void unlike(Long commentId, Long userId) {
        var opt = likeRepo.findByComment_IdAndUser_Id(commentId, userId);
        if (opt.isEmpty()) return;

        CommentLike like = opt.get();
        likeRepo.delete(like);

        Comment c = like.getComment();
        int current = c.getLikeCount() == null ? 0 : c.getLikeCount();
        c.setLikeCount(Math.max(0, current - 1));
    }
}
