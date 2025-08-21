package phankhanh.book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phankhanh.book_store.Repository.EmailVerificationRepository;
import phankhanh.book_store.Repository.RoleRepository;
import phankhanh.book_store.Repository.UserRepository;
import phankhanh.book_store.domain.EmailVerification;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.util.error.EmailAlreadyExistsException;
import phankhanh.book_store.util.error.InvalidOtpException;
import phankhanh.book_store.util.error.UsernameAlreadyExistsException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository UserRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    private static final Duration EXPIRE = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

    private String genOtp () {
        var random = new SecureRandom();
        int code = random.nextInt(900000) + 100000; //
        return Integer.toString(code);
    }

    @Transactional
    public void startRegister(String email, String password, String username, String fullName, String phone) throws EmailAlreadyExistsException, UsernameAlreadyExistsException {
        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if(userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        //gen OTP
        var now = Instant.now();
        var otp = genOtp();
        var otpHash = passwordEncoder.encode(otp);

        var existing = emailVerificationRepository.findByEmailAndPurposeAndUsedFalse(email, "REGISTER").orElse(null);
        if (existing != null) {
            // cooldown resend
            if (Duration.between(existing.getLastSentAt(), now).compareTo(RESEND_COOLDOWN) < 0) {
                throw new IllegalArgumentException("Please wait before requesting another OTP");
            }
            existing.setOtpHash(otpHash);
            existing.setExpiresAt(now.plus(EXPIRE));
            existing.setAttempts(0);
            existing.setLastSentAt(now);
            emailVerificationRepository.save(existing);
        } else {
            var ev = EmailVerification.builder()
                    .email(email)
                    .otpHash(otpHash)
                    .purpose("REGISTER")
                    .expiresAt(now.plus(EXPIRE))
                    .attempts(0)
                    .maxAttempts(5)
                    .resendCount(0)
                    .lastSentAt(now)
                    .used(false)
                    .createdAt(now)
                    .build();
            emailVerificationRepository.save(ev);
        }
        mailService.sendOtp(email, otp);

    }

    @Transactional
    public Long verifyRegister(String email, String otp, String rawPassword,String username, String fullName, String phone) throws InvalidOtpException, EmailAlreadyExistsException {
        var ev = emailVerificationRepository.findByEmailAndPurposeAndUsedFalse(email, "REGISTER")
                .orElseThrow(() -> new InvalidOtpException("OTP not found"));

        var now = Instant.now();
        if (ev.getExpiresAt().isBefore(now)) {
            emailVerificationRepository.delete(ev);
            throw new InvalidOtpException("OTP expired");
        }
        if (ev.getAttempts() >= ev.getMaxAttempts()) {
            emailVerificationRepository.delete(ev);
            throw new InvalidOtpException("Too many attempts");
        }

        ev.setAttempts(ev.getAttempts() + 1);
        emailVerificationRepository.save(ev);

        if (!passwordEncoder.matches(otp, ev.getOtpHash())) {
            throw new InvalidOtpException("OTP invalid");
        }

        // OTP đúng: mark used
        ev.setUsed(true);
        emailVerificationRepository.save(ev);

        // Tạo user (ManyToOne Role)
        if (userRepository.existsByEmail(email)) {
            // Trường hợp race condition: email vừa được tạo ở nơi khác
            throw new EmailAlreadyExistsException("Email already exists");
        }
        var roleUser = roleRepository.findByName("ROLE_USER").orElseThrow();
        var user = User.builder()
                .email(email)
                .username(username)
                .fullName(fullName)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(roleUser)
                .enabled(true)
                .emailActive("ACTIVE") // Set email as active
                .build();
        return userRepository.save(user).getId();
    }

    @Transactional
    public void resendOtp(String email) throws InvalidOtpException {
        var ev = emailVerificationRepository.findByEmailAndPurposeAndUsedFalse(email, "REGISTER")
                .orElseThrow(() -> new InvalidOtpException("No pending OTP for this email"));

        var now = Instant.now();
        if (Duration.between(ev.getLastSentAt(), now).compareTo(RESEND_COOLDOWN) < 0) {
            throw new InvalidOtpException("Please wait before requesting another OTP");
        }
        if (ev.getResendCount() >= 5) {
            throw new InvalidOtpException("Resend limit reached");
        }

        var otp = genOtp();
        ev.setOtpHash(passwordEncoder.encode(otp));
        ev.setExpiresAt(now.plus(EXPIRE));
        ev.setLastSentAt(now);
        ev.setResendCount(ev.getResendCount() + 1);
        ev.setAttempts(0);
        emailVerificationRepository.save(ev);
        mailService.sendOtp(email, otp);
    }

    @Transactional
    public void startForgotPassword(String email) {
        var userOtp = this.UserRepository.findByEmail(email);
        if(userOtp.isEmpty()) return;
        User user = userOtp.get();

        if (user.getDeletedAt() != null || !user.isEnabled() || !"ACTIVE".equals(user.getEmailActive())) {
            return;
        }
        this.emailVerificationRepository.deleteAllByEmailAndPurpose(email, "FORGOT");

        // Generate OTP
        var otp = genOtp();
        var otpHash = this.passwordEncoder.encode(otp);
        var ev = EmailVerification.builder()
                .email(email)
                .purpose("FORGOT")
                .otpHash(otpHash)
                .attempts(0)
                .maxAttempts(5)
                .used(false)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build()
                ;
        this.emailVerificationRepository.save(ev);
        this.mailService.sendOtp(email, "Your OTP for password reset is: " + otp + " Do not share this OTP with anyone.");
    }

    @Transactional
    public void verifyForgotPassword(String email, String otp, String newPassword) {
        var ev = this.emailVerificationRepository.findByEmailAndPurposeAndUsedFalse(email, "FORGOT")
                .orElseThrow(() -> new InvalidOtpException("OTP not found"));
        var now = Instant.now();
        if(ev.getExpiresAt().isBefore(now)) {
            this.emailVerificationRepository.delete(ev);
            throw new InvalidOtpException("OTP expired");
        }
        if (ev.getAttempts() >= ev.getMaxAttempts()) {
            emailVerificationRepository.delete(ev);
            throw new InvalidOtpException("Too many attempts");
        }
        ev.setAttempts(ev.getAttempts() + 1);
        this.emailVerificationRepository.save(ev);

        if(!passwordEncoder.matches(otp, ev.getOtpHash())) {
            throw new InvalidOtpException("OTP invalid");
        }

        //OTP Matches: mark used
        ev.setUsed(true);
        this.emailVerificationRepository.save(ev);

        // Update user password
        var user = this.UserRepository.findByEmail(email).orElseThrow();
        if (user.getDeletedAt() != null || !user.isEnabled() || !"ACTIVE".equals(user.getEmailActive())) {
            throw new IllegalStateException("Account inactive");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);

        this.refreshTokenService.revokeAllActiveByUserId(user.getId());
    }


}
