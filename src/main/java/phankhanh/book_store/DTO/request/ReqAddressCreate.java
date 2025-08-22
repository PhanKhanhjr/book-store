package phankhanh.book_store.DTO.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReqAddressCreate(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Size(max = 255) String line1,
        @NotBlank @Size(max = 100) String ward,
        @NotBlank @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String province,
        Boolean makeDefault
) {
}
