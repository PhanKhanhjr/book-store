package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReqRegister(
        @NotBlank(message = "Email cannot be empty") @Email
        String email,
        @NotBlank(message = "Password cannot be empty") @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
        String password,
        String username,
        String fullName,
        String phone
) {
}
