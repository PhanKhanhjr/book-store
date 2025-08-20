package phankhanh.book_store.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

// util/CookieUtils.java
public final class CookieUtil {
    private CookieUtil() {}

    public static ResponseCookie buildRefreshCookie(String token, Duration maxAge, boolean prod) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(prod)                 // prod=true => cần HTTPS
                .path("/")
                .sameSite(prod ? "None" : "Lax") //Dev FE (http://localhost:5173) & BE (http://localhost:8080) thường vẫn coi là same-site ⇒ Lax ok Prod nếu FE khác domain ⇒ phải SameSite=None; Secure (bắt buộc HTTPS)
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie deleteRefreshCookie(boolean prod) {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(prod)
                .path("/")
                .sameSite(prod ? "None" : "Lax")
                .maxAge(Duration.ZERO)
                .build();
    }
}

