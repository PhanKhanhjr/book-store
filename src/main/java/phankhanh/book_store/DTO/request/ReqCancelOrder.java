package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record ReqCancelOrder(@NotBlank String reason) {}