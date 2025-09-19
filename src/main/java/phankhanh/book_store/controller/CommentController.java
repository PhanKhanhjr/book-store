package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqCreateComment;
import phankhanh.book_store.DTO.request.ReqUpdateComment;
import phankhanh.book_store.DTO.response.ResComment;
import phankhanh.book_store.service.CommentService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService service;

    private Long currentUserId(Jwt jwt) {
        return jwt.getClaim("userId");
    }

    private boolean isAdmin(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.stream().anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    @GetMapping("/books/{bookId}/comments")
    public ResponseEntity<Map<String, Object>> list(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "new") String sort,
            @RequestParam(defaultValue = "false") boolean includeHidden,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var direction = "old".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        boolean admin = jwt != null && isAdmin(jwt);
        return ResponseEntity.ok(service.listByBook(bookId, pageable, includeHidden, admin));
    }

    @PostMapping("/books/{bookId}/comments")
    public ResponseEntity<ResComment> create(
            @PathVariable Long bookId,
            @RequestBody ReqCreateComment req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(this.service.create(bookId, currentUserId(jwt), req));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<ResComment> update(
            @PathVariable Long id,
            @RequestBody ReqUpdateComment req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.update(id, currentUserId(jwt), isAdmin(jwt), req));

    }
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        service.delete(id, currentUserId(jwt), isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/comments/{id}/visibility")
    public ResponseEntity<Void> hideOrUnhide(
            @PathVariable Long id,
            @RequestParam boolean hide
    ) {
        service.hideOrUnhide(id, hide);
        return ResponseEntity.noContent().build();
    }
}
