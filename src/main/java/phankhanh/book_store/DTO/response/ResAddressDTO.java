package phankhanh.book_store.DTO.response;

import java.time.Instant;

public record ResAddressDTO(
        Long id,
        Long userId,
        String fullName,
        String phone,
        String line1,
        String ward,
        String district,
        String province,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {}
