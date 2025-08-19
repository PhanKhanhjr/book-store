package phankhanh.book_store.DTO.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;

public record UserFilter(
        String q,
        String email,
        String username,
        String fullName,
        String phoneNumber,
        String roleName,   // hỗ trợ ADMIN / ROLE_ADMIN
        String gender,     // FEMALE/MALE/OTHER
        Boolean enabled,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDateTo,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo
) {}
