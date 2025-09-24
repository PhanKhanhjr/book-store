package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.response.ResInventory;
import phankhanh.book_store.DTO.response.ResMovementLog;
import phankhanh.book_store.domain.Inventory;
import phankhanh.book_store.domain.InventoryMovement;
import phankhanh.book_store.service.InventoryService;
import phankhanh.book_store.DTO.request.ReqMovement;
import phankhanh.book_store.DTO.response.ResMovement;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    @PostMapping("/movements")
    public ResponseEntity<ResMovement> move(
            @RequestBody ReqMovement req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long actorId = jwt.getClaim("userId");
        return ResponseEntity.ok(inventoryService.applyMovement(req, actorId));
    }

    @GetMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResInventory> get(@PathVariable Long bookId) {
        Inventory inv = inventoryService.getInventory(bookId);
        return ResponseEntity.ok(new ResInventory(inv.getBook().getId(), inv.getStock(),inv.getSold()));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ResMovementLog> list(
            @RequestParam Long bookId,
            Pageable pageable
    ) {
        return inventoryService.listMovements(bookId, pageable);
    }
}
