package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.response.ResPublisher;
import phankhanh.book_store.domain.Publisher;
import phankhanh.book_store.repository.PublisherRepository;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class PublisherController {
    private final PublisherRepository repo;

    @PostMapping("/publishers")
    public ResponseEntity<Publisher> create(@RequestBody Publisher req) {
        return ResponseEntity.ok(repo.save(req));
    }

    @GetMapping("/publishers")
    public ResponseEntity<List<ResPublisher>> list() {
        List<ResPublisher> data = repo.findAll().stream()
                .map(p -> new ResPublisher(p.getId(), p.getName(), p.getSlug()))
                .toList();
        return ResponseEntity.ok(data);
    }
}

