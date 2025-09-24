package phankhanh.book_store.DTO.response;

public record ResInventory(
        Long bookId,
        Integer stock,
        Integer sold
) {}