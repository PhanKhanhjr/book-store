package phankhanh.book_store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.User;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"role", "address"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update User u set u.enabled=false, u.deletedAt=:now where u.id=:id and u.deletedAt is null")
    int softDelete(@Param("id") Long id, @Param("now") Instant now);
}
