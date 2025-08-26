package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.domain.Category;
import phankhanh.book_store.repository.CategoryRepository;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository repo;

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category req) {
        return ResponseEntity.ok(repo.save(req));
    }
    @GetMapping
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(repo.findAll());
    }
}

