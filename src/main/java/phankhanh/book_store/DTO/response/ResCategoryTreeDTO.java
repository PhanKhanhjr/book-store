package phankhanh.book_store.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResCategoryTreeDTO {
    private Long id;
    private String name;
    private String slug;
    private java.util.List<ResCategoryTreeDTO> children;
}
