package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReqForgotStart(
        @NotBlank(message = "Email is required")
        @Email
        String email
) {
}
