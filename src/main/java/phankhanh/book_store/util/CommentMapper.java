package phankhanh.book_store.util;

import phankhanh.book_store.DTO.response.ResComment;
import phankhanh.book_store.domain.Comment;

import java.util.List;

public final class CommentMapper {
    private CommentMapper() {}

    public static ResComment toDto(Comment c, List<ResComment> children,
                                   String userName, String userAvatar,Integer likeCount, Boolean likedByMe) {
        return new ResComment(
                c.getId(),
                c.getUser().getId(),
                userName,
                userAvatar,
                c.getContent(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getStatus().name(),
                likeCount == null ? c.getLikeCount() : likeCount,
                likedByMe,
                children
        );
    }
}
