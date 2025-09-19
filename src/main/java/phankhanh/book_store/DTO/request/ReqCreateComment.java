package phankhanh.book_store.DTO.request;

public record ReqCreateComment(
        String content,
        Long parentId
) {}
