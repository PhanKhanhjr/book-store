package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.Size;
import phankhanh.book_store.util.constant.GenderEnum;

import java.time.LocalDate;

public record ReqUserUpdate(
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        String fullName,
        String phone,
        LocalDate birthDate,
        String gender
) {
}
