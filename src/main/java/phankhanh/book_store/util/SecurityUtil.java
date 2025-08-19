package phankhanh.book_store.util;

import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SecurityUtil {
    @Value("${phankhanh.jwt.base64-secret}")
    private String jwtSecret;
    @Value("${phankhanh.jwt.refresh-token-validity-in-seconds}")
    private long jwtExpirationInSeconds;

    private final JwtEncoder jwtEncoder;
    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;


//    public String createToken (Authentication authentication) {
//        Instant now = Instant.now();
//        Instant validity = now.plus(this.jwtExpirationInSeconds, ChronoUnit.SECONDS);
//        var principal = (CustomUserDetails) authentication.getPrincipal();
//
//        JwtClaimsSet claims = JwtClaimsSet.builder()
//                .issuedAt(now)
//                .expiresAt(validity)
//                .subject(authentication.getName())
//                .claims("userId", principal.getId())
//                .build();
//        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
//        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
//    }
public String createToken(Authentication authentication) {
    Instant now = Instant.now();
    Instant exp  = now.plusSeconds(this.jwtExpirationInSeconds);

    // principal phải là CustomUserDetails để lấy được id
    var principal = (CustomUserDetails) authentication.getPrincipal();

    // convert authorities -> ["ROLE_ADMIN", "ROLE_USER"] (hoặc bỏ "ROLE_" tuỳ convention)
    var roles = principal.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(exp)
            .subject(principal.getUsername())
            .claim("userId", principal.getId())
            .claim("roles", roles)
            .build();
    var headers = JwsHeader.with(JWT_ALGORITHM).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(headers,claims)).getTokenValue();
}

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

}
