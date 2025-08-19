package phankhanh.book_store.DTO.response;

import java.time.Instant;
import java.time.LocalDate;

public record ResUserDTO (
        Long id,
        String email,
        String username,
        String fullName,
        String phone,
        LocalDate birthDate,
        String gender,       // FEMALE / MALE / OTHER
        String role,     // ROLE_USER / ROLE_ADMIN
        Instant createdAt,
        Instant updatedAt
) {}
