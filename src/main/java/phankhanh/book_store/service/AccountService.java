package phankhanh.book_store.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phankhanh.book_store.repository.UserRepository;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.util.error.IdInvalidException;

@Service
public class AccountService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    public AccountService(UserService userService, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) throws IdInvalidException {
        User user = userService.findUserById(userId);
        if(user.getDeletedAt() != null || user.isEnabled() == false || !"ACTIVE".equals(user.getEmailActive()) ) {
            throw new IllegalArgumentException("Account inactive or deleted");
        }

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if(passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
        // Revoke all refresh tokens for this user
        this.refreshTokenService.revokeAllActiveByUserId(userId);
    }
}
