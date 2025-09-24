package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.request.ReqMovement;
import phankhanh.book_store.DTO.response.ResMovement;
import phankhanh.book_store.DTO.response.ResMovementLog;
import phankhanh.book_store.domain.Inventory;
import phankhanh.book_store.domain.InventoryMovement;
import phankhanh.book_store.repository.InventoryMovementRepository;
import phankhanh.book_store.repository.InventoryRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepo;
    private final InventoryMovementRepository movementRepo;

        @Transactional
        public ResMovement applyMovement(ReqMovement req, Long actorId) {
            Inventory inv = inventoryRepo.findByBook_Id(req.bookId())
                    .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

            int oldQty = inv.getStock();
            int newQty;

            switch (InventoryMovement.MovementType.valueOf(req.type())) {
                case IN -> newQty = oldQty + req.quantity();
                case OUT -> {
                    if (oldQty < req.quantity())
                        throw new IllegalStateException("Not enough stock");
                    newQty = oldQty - req.quantity();
                }
                case ADJUST -> newQty = req.quantity();
                default -> throw new IllegalArgumentException("Invalid type");
            }

            inv.setStock(newQty);
            inventoryRepo.save(inv);
            InventoryMovement mv = InventoryMovement.builder()
                    .inventory(inv)
                    .type(InventoryMovement.MovementType.valueOf(req.type()))
                    .delta(newQty - oldQty)
                    .quantityAfter(newQty)
                    .reason(req.reason())
                    .createdAt(Instant.now())
                    .build();
            movementRepo.save(mv);
            return new ResMovement(req.bookId(), oldQty, mv.getDelta(), newQty, mv.getId());
        }

        public Inventory getInventory(Long bookId) {
            return inventoryRepo.findByBook_Id(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        }

    public Page<ResMovementLog> listMovements(Long bookId, Pageable pageable) {
        Page<InventoryMovement> page = movementRepo.findByInventory_Book_Id(bookId, pageable);
        return page.map(mv -> new ResMovementLog(
                mv.getId(),
                mv.getInventory().getBook().getId(),
                mv.getType().name(),
                mv.getDelta(),
                mv.getQuantityAfter(),
                mv.getReason(),
                mv.getCreatedAt()
        ));
    }

}