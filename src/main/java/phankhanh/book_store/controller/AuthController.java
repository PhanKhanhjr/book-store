package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqLoginDTO;
import phankhanh.book_store.DTO.request.ReqRegister;
import phankhanh.book_store.DTO.request.ReqResendOtp;
import phankhanh.book_store.DTO.request.ReqVerifyEmail;
import phankhanh.book_store.DTO.response.ResLoginDTO;
import phankhanh.book_store.service.AuthService;
import phankhanh.book_store.service.OtpService;
import phankhanh.book_store.util.SecurityUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final AuthService authService;
    private final OtpService otpService;
    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, AuthService authService, OtpService otpService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.authService = authService;
        this.otpService = otpService;
    }
    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> longin(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(reqLoginDTO.getUsername(), reqLoginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //Create token
        String accessToken = this.securityUtil.createToken(authentication);
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setAccessToken(accessToken);
        return ResponseEntity.ok().body(resLoginDTO);
    }

//    @PostMapping("/register")
//    public ResponseEntity<String> register(@Valid @RequestBody ReqRegister reqRegister) {
//        this.authService.register(
//                reqRegister.email(),
//                reqRegister.password(),
//                reqRegister.fullName(),
//                reqRegister.userName(),
//                reqRegister.phone()
//        );
//        return ResponseEntity.ok().body("Register successfully");
//    }
    @PostMapping("/register")
    public ResponseEntity<?> registerStart(@Valid @RequestBody ReqRegister req) {
        otpService.startRegister(req.email(), req.password(), req.fullName(), req.username(), req.phone());
    return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
}

    @PostMapping("/verify-email")
    public ResponseEntity<?> verify(@Valid @RequestBody ReqVerifyEmail req){
        Long id = otpService.verifyRegister(req.email(), req.otp(), req.password(), req.fullName(), req.username(), req.phone());
        return ResponseEntity.ok(Map.of("userId", id, "message", "verified"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resend(@Valid @RequestBody ReqResendOtp req) {
        otpService.resendOtp(req.email());
        return ResponseEntity.ok(Map.of("message", "OTP resent"));
    }
}
