package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqBookCreate;
import phankhanh.book_store.DTO.response.ResBookDetailDTO;
import phankhanh.book_store.DTO.response.ResBookListItemDTO;
import phankhanh.book_store.service.BookService;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<Page<ResBookListItemDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        var data = bookService.listAll(pageable);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/books/{slug}")
    public ResponseEntity<ResBookDetailDTO> getDetail(@PathVariable String slug) {
        var data = bookService.getDetail(slug);
        return ResponseEntity.ok(data);
    }

    //Admin
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/books")
    public ResponseEntity<ResBookDetailDTO> create(@Valid @RequestBody ReqBookCreate req) {
        var data = bookService.create(req);
        return ResponseEntity.ok(data);
    }
}
