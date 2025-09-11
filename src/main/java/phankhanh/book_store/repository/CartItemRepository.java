package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCart_IdAndBookId(Long cartId, Long bookId);
    List<CartItem> findByCart_Id(Long cartId);
}
