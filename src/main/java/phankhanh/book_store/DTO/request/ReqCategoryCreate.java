package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCategoryCreate {
    @NotBlank
    private String name;
    private Long parentId;     // optional
    private String parentSlug; // optional
}
