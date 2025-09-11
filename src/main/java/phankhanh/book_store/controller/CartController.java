package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ClearReq;
import phankhanh.book_store.DTO.request.ReqAddItem;
import phankhanh.book_store.DTO.request.ReqSelectAll;
import phankhanh.book_store.DTO.request.ReqUpdateItem;
import phankhanh.book_store.DTO.response.ResCartSummary;
import phankhanh.book_store.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ResCartSummary> get(@AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping("/items/{bookId}")
    public ResponseEntity<ResCartSummary> update(@AuthenticationPrincipal Jwt jwt,
                                 @PathVariable Long bookId,
                                 @RequestBody ReqUpdateItem req) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(cartService.updateItem(userId, bookId, req));
    }

    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<ResCartSummary> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable Long bookId) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(cartService.removeItem(userId, bookId));
    }

    @PostMapping("/clear")
    public ResponseEntity<ResCartSummary> clear(@AuthenticationPrincipal Jwt jwt,
                                @RequestBody(required=false) ClearReq req) {
        Long userId = jwt.getClaim("userId");
        boolean onlyUnselected = (req != null && Boolean.TRUE.equals(req.onlyUnselected()));
        return ResponseEntity.ok(cartService.clear(userId, onlyUnselected));
    }

    @PostMapping("/items")
    public ResponseEntity<ResCartSummary> add(@AuthenticationPrincipal Jwt jwt, @RequestBody ReqAddItem req) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(cartService.addItem(userId, req));
    }

    @PutMapping("/select-all")
    public ResponseEntity<ResCartSummary> selectAll(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody(required = false) ReqSelectAll req) {
        Long userId = jwt.getClaim("userId");
        boolean selected = (req == null || req.selected() == null) ? true : req.selected();
        return ResponseEntity.ok(cartService.selectAll(userId, selected));
    }
}
