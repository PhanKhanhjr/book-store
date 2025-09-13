package phankhanh.book_store.DTO.response;

public record ResCategoryFlat(
        Long id,
        String name,
        String slug,
        Long parentId,
        boolean leaf
) {}
