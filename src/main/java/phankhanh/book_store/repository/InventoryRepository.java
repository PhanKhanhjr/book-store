package phankhanh.book_store.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.Inventory;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBook_Id(Long bookId);

    @Modifying
    @Query("update Inventory i set i.stock = i.stock - :qty, i.sold = i.sold + :qty where i.book.id = :bookId and i.stock >= :qty")
    int decreaseStockAndIncreaseSold(@Param("bookId") Long bookId, @Param("qty") int qty);

    @Modifying
    @Query("update Inventory i set i.stock = i.stock + :qty, i.sold = i.sold - :qty where i.book.id = :bookId and i.sold >= :qty")
    int rollbackStockAndSold(@Param("bookId") Long bookId, @Param("qty") int qty);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.book.id = :bookId")
    Optional<Inventory> findByBookIdForUpdate(@Param("bookId") Long bookId);
}
