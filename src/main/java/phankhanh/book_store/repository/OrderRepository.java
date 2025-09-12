package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCode(String code);
    @EntityGraph(attributePaths = "items")
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
