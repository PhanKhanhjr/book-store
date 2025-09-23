package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqCategoryCreate;
import phankhanh.book_store.DTO.response.CategoryDTO;
import phankhanh.book_store.DTO.response.ResCategoryFlat;
import phankhanh.book_store.DTO.response.ResCategoryTreeDTO;
import phankhanh.book_store.domain.Category;
import phankhanh.book_store.repository.CategoryRepository;
import phankhanh.book_store.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDTO> create(@RequestBody ReqCategoryCreate req) {
        return ResponseEntity.ok(categoryService.create(req));
    }
    private ResCategoryTreeDTO toTreeDTO(Category c) {
        var kids = c.getChildren() == null ? java.util.List.<ResCategoryTreeDTO>of()
                : c.getChildren().stream().map(this::toTreeDTO).toList();
        return new ResCategoryTreeDTO(c.getId(), c.getName(), c.getSlug(), kids);
    }

    @GetMapping("/categories/tree")
    public List<ResCategoryTreeDTO> tree() {
        var roots = categoryRepository.findAllByParentIsNull(); // viết repo method này
        return roots.stream().map(this::toTreeDTO).toList();
    }
    @GetMapping("/categories/flat")
    public ResponseEntity<List<ResCategoryFlat>> getFlatAll() {
        var data = categoryRepository.findAllActive().stream()
                .map(c -> new ResCategoryFlat(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getParent() != null ? c.getParent().getId() : null,
                        c.getChildren() == null || c.getChildren().isEmpty()
                ))
                .toList();
        return ResponseEntity.ok(data);
    }
    @GetMapping("/categories/flat/leaf")
    public ResponseEntity<List<ResCategoryFlat>> getFlatLeaf() {
        var data = categoryRepository.findAllLeafActive().stream()
                .map(c -> new ResCategoryFlat(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getParent() != null ? c.getParent().getId() : null,
                        true
                ))
                .toList();
        return ResponseEntity.ok(data);
    }
}

