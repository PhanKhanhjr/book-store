package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReqVerifyEmail(
        @Email @NotBlank(message = "Email cannot be empty")
        String email,
        @NotBlank(message = "Verification code cannot be empty")
        String otp,
        @NotBlank(message = "Password cannot be empty")
        String password,
        String fullName,
        String username,
        String phone
) {
}
