package phankhanh.book_store.DTO.response;

import java.time.Instant;
public record ResRating(
        Long id, Long userId, String userName,
        Integer score,String content,
        Instant createdAt, Instant updatedAt
) {}
