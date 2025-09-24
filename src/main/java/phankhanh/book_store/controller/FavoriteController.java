package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.response.ResBookFavoriteDTO;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.service.FavoriteService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/books/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{bookId}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        boolean liked = favoriteService.toggleFavorite(userId, bookId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @GetMapping("/me")
    public Page<ResBookFavoriteDTO> myFavorites(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        Long userId = jwt.getClaim("userId");
        return favoriteService.getUserFavorites(userId, pageable);
    }
}