package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long>, JpaSpecificationExecutor<EmailVerification> {
    Optional<EmailVerification> findByEmailAndPurposeAndUsedFalse(String email, String purpose);

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.email = :email AND e.purpose = :purpose")
    void deleteAllByEmailAndPurpose(@Param("email") String email, @Param("purpose") String purpose);
}
