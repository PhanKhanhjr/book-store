package phankhanh.book_store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phankhanh.book_store.repository.RefreshTokenRepository;
import phankhanh.book_store.repository.UserRepository;
import phankhanh.book_store.domain.RefreshToken;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.util.TokenUtil;

import java.time.Duration;
import java.time.Instant;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    @Transactional
    public String issueForUser(Long userId, boolean revokeOld) {
        User user = this.userRepository.findById(userId).orElse(null);
        if (revokeOld) {
            this.refreshTokenRepository.revokeAllActiveByUserId(userId);
        }
        String plain = TokenUtil.newOpaqueToken();
        String hash = TokenUtil.sha256Hex(plain);
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(Instant.now().plus(REFRESH_TTL))
                .revoked(false)
                .build();
        this.refreshTokenRepository.save(rt);
        return plain;
    }

    @Transactional
    public Long verifyAndRotate (String plainToken) {
        String hash = TokenUtil.sha256Hex(plainToken);
        RefreshToken current = this.refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow( () -> new IllegalArgumentException("Invalid refresh token") );

        if (current.getExpiresAt().isBefore(Instant.now())) {
            current.setRevoked(true);
            this.refreshTokenRepository.save(current);
            throw new IllegalArgumentException("Refresh token expired");
        }
        //rotation: tao token moi, revoke token cu, link replaced by
        String nextPlain = TokenUtil.newOpaqueToken();
        String nextHash = TokenUtil.sha256Hex(nextPlain);

        RefreshToken next = RefreshToken.builder()
                .user(current.getUser())
                .tokenHash(nextHash)
                .expiresAt(Instant.now().plus(REFRESH_TTL))
                .revoked(false)
                .replacedBy(hash) // link to the old token
                .build();
        this.refreshTokenRepository.save(next);
        current.setRevoked(true); // revoke the old token
        current.setReplacedBy(nextHash);
        this.refreshTokenRepository.save(current);

        this.rotatedPlain.set(nextPlain);
        return current.getUser().getId();
    }

    private final ThreadLocal<String> rotatedPlain = new ThreadLocal<>();
    public String getRotatedPlainAndClear() {
        String v = rotatedPlain.get();
        rotatedPlain.remove();
        return v;
    }

    @Transactional
    public void revokeToken(String plainToken) {
        String hash = TokenUtil.sha256Hex(plainToken);
        this.refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            this.refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revokeAllActiveByUserId(Long userId) {
        this.refreshTokenRepository.revokeAllActiveByUserId(userId);
    }
}
