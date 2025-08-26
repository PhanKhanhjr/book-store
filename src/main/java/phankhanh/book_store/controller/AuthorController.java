package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.domain.Author;
import phankhanh.book_store.repository.AuthorRepository;

import java.util.List;

@RestController
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorRepository repo;

    @PostMapping
    public ResponseEntity<Author> create(@RequestBody Author req) {
        return ResponseEntity.ok(repo.save(req));
    }
    @GetMapping
    public ResponseEntity<List<Author>> list() {
        return ResponseEntity.ok(repo.findAll());
    }
}
