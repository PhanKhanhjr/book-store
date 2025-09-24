package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import phankhanh.book_store.domain.InventoryMovement;

import java.time.Instant;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // Lấy lịch sử biến động của 1 book theo inventory_id
    Page<InventoryMovement> findByInventory_Id(Long inventoryId, Pageable pageable);

    //lấy tất cả biến động trong 1 khoảng thời gian
    List<InventoryMovement> findByCreatedAtBetween(Instant from, Instant to);

    Page<InventoryMovement> findByInventory_Book_Id(Long bookId, Pageable pageable);
}

