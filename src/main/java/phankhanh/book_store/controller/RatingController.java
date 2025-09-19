package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqRateBook;
import phankhanh.book_store.DTO.response.ResRating;
import phankhanh.book_store.DTO.response.ResRatingSummary;
import phankhanh.book_store.service.RatingService;

import java.util.Optional;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService service;

    private Long uid(Jwt jwt) { return jwt.getClaim("userId"); }
    private boolean isAdmin(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    @GetMapping("/books/{bookId}/ratings/summary")
    public ResponseEntity<ResRatingSummary> summary(@PathVariable Long bookId) {
        return ResponseEntity.ok(service.summary(bookId));
    }

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<?> reviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "new") String sort // new|old|high|low
    ) {
        Sort s = switch (sort.toLowerCase()) {
            case "old" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "high" -> Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "low"  -> Sort.by(Sort.Direction.ASC,  "score").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        return ResponseEntity.ok(service.listReviews(bookId, PageRequest.of(page, size, s)));
    }

    @PostMapping("/books/{bookId}/ratings")
    public ResponseEntity<ResRating> upsert(
            @PathVariable Long bookId,
            @RequestBody ReqRateBook req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.upsert(bookId, uid(jwt), req));
    }

    //
    @GetMapping("/books/{bookId}/ratings/me")
    public ResponseEntity<Optional<ResRating>> myRating(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(service.myRating(bookId, uid(jwt)));
    }

    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, uid(jwt), isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

}
