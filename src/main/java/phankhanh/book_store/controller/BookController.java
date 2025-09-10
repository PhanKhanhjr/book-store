package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqBookCreate;
import phankhanh.book_store.DTO.request.ReqBookUpdate;
import phankhanh.book_store.DTO.response.ResBookDetailDTO;
import phankhanh.book_store.DTO.response.ResBookListItemDTO;
import phankhanh.book_store.DTO.response.ResHomeFeedDTO;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.service.BookService;
import phankhanh.book_store.util.BookMapper;
import phankhanh.book_store.util.constant.AgeRating;
import phankhanh.book_store.util.constant.Language;
import phankhanh.book_store.util.constant.ProductStatus;

import java.math.BigDecimal;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    /* ===== PUBLIC: LIST (KHÔNG SEARCH) ===== */
    @GetMapping("/books")
    public ResponseEntity<Page<ResBookListItemDTO>> list(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        var data = bookService.list(status, authorId, categoryId, publisherId, supplierId, pageable);
        return ResponseEntity.ok(data);
    }

    /* ===== PUBLIC: SEARCH (CẦN q) ===== */
    @GetMapping("/books/search")
    public ResponseEntity<Page<ResBookListItemDTO>> search(
            @RequestParam String q,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        var data = bookService.search(q, status, authorId, categoryId, publisherId, supplierId, pageable);
        return ResponseEntity.ok(data);
    }

    /* ===== PUBLIC: HOME FEED (nhiều block) ===== */
    @GetMapping("/books/home")
    public ResponseEntity<ResHomeFeedDTO> home(
            @RequestParam(defaultValue = "ACTIVE") ProductStatus status,
            @RequestParam(defaultValue = "8")  int featuredSize,     // on-sale / featured
            @RequestParam(defaultValue = "12") int newSize,
            @RequestParam(defaultValue = "12") int bestSize,
            @RequestParam(required = false)    java.util.List<Long> categoryIds,
            @RequestParam(defaultValue = "8")  int perCategory
    ) {
        var data = bookService.buildHomeFeed(status, featuredSize, newSize, bestSize, categoryIds, perCategory);
        return ResponseEntity.ok(data);
    }

    // Get detail by id
    @GetMapping("/books/{id}")
    public ResponseEntity<ResBookDetailDTO> getById(@PathVariable Long id) {
        var data = bookService.getById(id);
        return ResponseEntity.ok(data);
    }

    // Get detail by slug (đường dẫn tách riêng để không đụng /{id})
    @GetMapping("/books/slug/{slug}")
    public ResponseEntity<ResBookDetailDTO> getBySlug(@PathVariable String slug) {
        var data = bookService.getBySlug(slug);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/books/catalog")
    public ResponseEntity<Page<ResBookListItemDTO>> filterBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) Language language,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) AgeRating ageMin,
            @RequestParam(required = false) AgeRating ageMax,
            // Hoặc tuổi theo số năm (tùy chọn)
            @RequestParam(required = false) Integer ageMinYears,
            @RequestParam(required = false) Integer ageMaxYears,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<Book> books = bookService.filterBooks(category, publisher, supplier, language, status,priceMin, priceMax, ageMin, ageMax,ageMinYears,ageMaxYears,  pageable);
        Page<ResBookListItemDTO> result = books.map(BookMapper::toListItem);
        return ResponseEntity.ok(result);
    }


    // Admin
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/books")
    public ResponseEntity<ResBookDetailDTO> create(@Valid @RequestBody ReqBookCreate req) {
        var data = bookService.create(req);
        return ResponseEntity.ok(data);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/admin/books/{id}")
    public ResponseEntity<ResBookDetailDTO> update(@PathVariable Long id,
                                                   @Valid @RequestBody ReqBookUpdate req) {
        var data = bookService.update(id, req);
        return ResponseEntity.ok(data);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/books/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
