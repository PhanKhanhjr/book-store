package phankhanh.book_store.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phankhanh.book_store.repository.RoleRepository;
import phankhanh.book_store.repository.UserRepository;
import phankhanh.book_store.domain.Role;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.util.error.EmailAlreadyExistsException;
import phankhanh.book_store.util.error.UsernameAlreadyExistsException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long register(String email, String rawPassword, String fullName, String username, String phone) throws EmailAlreadyExistsException, UsernameAlreadyExistsException {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        Role roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Missing ROLE_USER. Seed roles first."));

        phankhanh.book_store.domain.User u = User.builder()
                .email(email)
                .username(username)
                .fullName(fullName)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(roleUser)          // ManyToOne
                .build();
        return userRepository.save(u).getId();
    }
}
