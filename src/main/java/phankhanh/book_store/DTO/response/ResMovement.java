package phankhanh.book_store.DTO.response;

public record ResMovement(
        Long bookId,
        Integer oldQuantity,
        Integer delta,
        Integer newQuantity,
        Long movementId
) {}