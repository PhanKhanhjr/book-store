package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}