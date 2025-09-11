package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}