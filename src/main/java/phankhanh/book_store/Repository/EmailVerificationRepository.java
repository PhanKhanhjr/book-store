package phankhanh.book_store.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import phankhanh.book_store.domain.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long>, JpaSpecificationExecutor<EmailVerification> {
    Optional<EmailVerification> findByEmailAndPurposeAndUsedFalse(String email, String purpose);
}
