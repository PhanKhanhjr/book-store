package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByNameIgnoreCase(String name);
    List<Supplier> findByNameContainingIgnoreCase(String keyword);
    boolean existsByNameIgnoreCase(String name);
}
