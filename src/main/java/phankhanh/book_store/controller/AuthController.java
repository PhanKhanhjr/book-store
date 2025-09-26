package phankhanh.book_store.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.*;
import phankhanh.book_store.DTO.response.ResLoginDTO;
import phankhanh.book_store.DTO.response.RestResponse;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.service.AuthService;
import phankhanh.book_store.service.OtpService;
import phankhanh.book_store.service.RefreshTokenService;
import phankhanh.book_store.service.UserService;
import phankhanh.book_store.util.CookieUtil;
import phankhanh.book_store.util.SecurityUtil;
import phankhanh.book_store.util.anotation.ApiMessage;
import phankhanh.book_store.util.error.IdInvalidException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final AuthService authService;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    @Value("${phankhanh.app.prod}")
    private boolean prod;
    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, AuthService authService, OtpService otpService, RefreshTokenService refreshTokenService, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.authService = authService;
        this.otpService = otpService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }
    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> longin(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(reqLoginDTO.getUsername(), reqLoginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //Create token
        String accessToken = this.securityUtil.createToken(authentication);
        // get userId
        Long userId = ((phankhanh.book_store.util.CustomUserDetails) authentication.getPrincipal()).getId();
        // issue refresh token
        String refreshPlain = this.refreshTokenService.issueForUser(userId, true);
        ResponseCookie cookie = CookieUtil.buildRefreshCookie(refreshPlain, Duration.ofDays(7), prod);

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setAccessToken(accessToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resLoginDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerStart(@Valid @RequestBody ReqRegister req) {
        otpService.startRegister(req.email(), req.password(),req.username(), req.fullName(),  req.phone());
    return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
}
    @PermitAll
    @PostMapping("/verify-email")
    public ResponseEntity<?> verify(@Valid @RequestBody ReqVerifyEmail req){
        Long id = otpService.verifyRegister(req.email(), req.otp(), req.password(), req.username(),req.fullName(),  req.phone());
        return ResponseEntity.ok(Map.of("userId", id, "message", "verified"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resend(@Valid @RequestBody ReqResendOtp req) {
        otpService.resendOtp(req.email());
        return ResponseEntity.ok(Map.of("message", "OTP resent"));
    }

    @PostMapping("/refresh")
    @ApiMessage("Refresh access token using refresh token")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshCookie) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            return ResponseEntity.status(401).body(Map.of(
                    "statusCode", 401, "error", "Unauthorized", "message", "Missing refresh token", "data", null));
        }
        try{
            //verify and rotate
            Long userId = this.refreshTokenService.verifyAndRotate(refreshCookie);
            String nextPlain = this.refreshTokenService.getRotatedPlainAndClear();
            User user = this.userService.findUserById(userId);
            String accessToken = this.securityUtil.createToken(
                    user.getEmail(),
                    user.getId(),
                    List.of(user.getRole().getName())
            );
            var cookie = CookieUtil.buildRefreshCookie(nextPlain, Duration.ofDays(7), prod);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("accessToken", accessToken));
        } catch (IllegalArgumentException e) {
            var del = CookieUtil.deleteRefreshCookie(prod);
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, del.toString())
                    .body(Map.of("statusCode", 401, "error", "Unauthorized", "message", e.getMessage(), "data", null));
        } catch (IdInvalidException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshCookie) {
        if(refreshCookie !=null && refreshCookie.isBlank()){
            this.refreshTokenService.revokeToken(refreshCookie);
        }
        ResponseCookie del = CookieUtil.deleteRefreshCookie(prod);
        RestResponse<Void> res = new RestResponse<>();
        res.setStatusCode(200);
        res.setMessage("LOGOUT_SUCCESS");
        res.setData(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, del.toString())
                .body(res);
    }

    @PostMapping("/forgot-password/start")
    @ApiMessage("Start forgot password process, send OTP to email")
    public ResponseEntity<?> forgotPasswordStart(@Valid @RequestBody ReqForgotStart req) {
        this.otpService.startForgotPassword(req.email());
        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    @PostMapping("/forgot-password/verify")
    @ApiMessage("Verify OTP and reset password")
    public ResponseEntity<?> forgotPasswordVerify(@Valid @RequestBody ReqForgotVerify req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password confirmation does not match"));
        }
        this.otpService.verifyForgotPassword(req.email(), req.otp(), req.newPassword());

        ResponseCookie del = CookieUtil.deleteRefreshCookie(prod);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, del.toString())
                .body(Map.of("message", "Password reset successfully, please login again"));
    }
}
