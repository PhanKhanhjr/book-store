package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_Code(String code);
}
