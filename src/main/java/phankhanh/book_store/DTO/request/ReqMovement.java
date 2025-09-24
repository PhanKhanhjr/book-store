package phankhanh.book_store.DTO.request;

public record ReqMovement(
        Long bookId,
        String type,       // IN | OUT | ADJUST
        Integer quantity,
        String reason
) {}
