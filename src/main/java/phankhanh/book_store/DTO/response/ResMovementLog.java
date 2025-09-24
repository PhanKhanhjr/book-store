package phankhanh.book_store.DTO.response;

import java.time.Instant;

public record ResMovementLog(
        Long id,
        Long bookId,
        String type,
        Integer delta,
        Integer quantityAfter,
        String reason,
        Instant createdAt
) {}