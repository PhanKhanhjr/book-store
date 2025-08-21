package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReqForgotVerify(
        @NotBlank(message = "Email is required")
        @Email
        String email,
        @NotBlank
        String otp,
        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters long" )
        String newPassword,
        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}
