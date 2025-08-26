package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.response.RestResponse;
import phankhanh.book_store.domain.Publisher;
import phankhanh.book_store.repository.PublisherRepository;

import java.util.List;

@RestController
@RequestMapping("/admin/publishers")
@RequiredArgsConstructor
public class PublisherController {
    private final PublisherRepository repo;

    @PostMapping
    public ResponseEntity<Publisher> create(@RequestBody Publisher req) {
        return ResponseEntity.ok(repo.save(req));
    }

    @GetMapping
    public ResponseEntity<List<Publisher>> list() {
        return ResponseEntity.ok(repo.findAll());
    }
}

