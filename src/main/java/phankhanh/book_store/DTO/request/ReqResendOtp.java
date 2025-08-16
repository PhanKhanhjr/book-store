package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReqResendOtp(
        @Email @NotBlank(message = "Email cannot be empty")
        String email
) {
}
