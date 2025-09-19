package phankhanh.book_store.DTO.response;

import java.time.Instant;
import java.util.List;

public record ResComment(
        Long id,
        Long userId,
        String userName,
        String userAvatar,
        String content,
        Instant createdAt,
        Instant updatedAt,
        Long parentId,
        String status,
        List<ResComment> children
) {}