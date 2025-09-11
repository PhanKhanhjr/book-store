package phankhanh.book_store.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private List<CategoryDTO> children;
}

