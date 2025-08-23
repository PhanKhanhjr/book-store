package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import phankhanh.book_store.domain.Publisher;

import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    Optional<Publisher> findByNameIgnoreCase(String name);
    List<Publisher> findByNameContainingIgnoreCase(String keyword);
    boolean existsByNameIgnoreCase(String name);
}

