package phankhanh.book_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.response.ResPaymentCheckout;
import phankhanh.book_store.service.VNPayService;
import phankhanh.book_store.util.constant.PaymentMethod;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService paymentService;

    @PostMapping("/{orderCode}/checkout")
    public ResPaymentCheckout checkout(@PathVariable String orderCode,
                                       @RequestParam PaymentMethod provider,
                                       @RequestParam(required = false) String returnUrl,
                                       HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        return paymentService.createCheckout(orderCode, provider, returnUrl, clientIp);
    }

    // IPN: VNPAY gọi server->server (PHẢI DÙNG RAW QUERY STRING)
    @GetMapping("/vnpay/ipn")
    public Map<String, String> vnpIpn(HttpServletRequest request) {
        String raw = request.getQueryString(); // raw, chưa decode
        log.info("raw: {hello}", raw);
        return paymentService.handleVnpayIpnRaw(raw);
    }

    // Return: user redirect về để FE hiển thị (dùng raw query cho đồng bộ)
    @GetMapping("/vnpay/return")
    public Map<String, Object> vnpReturn(HttpServletRequest request) {
        String raw = request.getQueryString();
        return paymentService.handleVnpayReturnRaw(raw);
    }

    private static String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // có thể là "client, proxy1, proxy2"
            int idx = ip.indexOf(',');
            return (idx > 0) ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip.trim();
        return request.getRemoteAddr();
    }
}
