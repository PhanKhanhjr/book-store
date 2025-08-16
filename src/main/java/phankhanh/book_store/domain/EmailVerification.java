package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "email_verifications")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
public class EmailVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable=false, length=255)
    private String email;

    @Column(name="otp_hash", nullable=false, length=255)
    private String otpHash;

    @Column(nullable=false, length=30)
    private String purpose; // "REGISTER"

    @Column(nullable=false)
    private Instant expiresAt;

    @Builder.Default
    private int attempts = 0;

    @Builder.Default
    private int maxAttempts = 5;

    @Builder.Default
    private int resendCount = 0;

    @Column(nullable=false)
    private Instant lastSentAt;

    @Builder.Default
    private boolean used = false;

    @Column(nullable=false)
    private Instant createdAt;
}
