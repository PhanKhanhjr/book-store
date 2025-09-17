package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.response.ResPaymentCheckout;
import phankhanh.book_store.domain.Order;
import phankhanh.book_store.domain.Payment;
import phankhanh.book_store.repository.OrderRepository;
import phankhanh.book_store.repository.PaymentRepository;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentMethod;
import phankhanh.book_store.util.constant.PaymentStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;

    @Value("${vnpay.tmnCode}")    private String vnpTmnCode;
    @Value("${vnpay.hashSecret}") private String vnpHashSecret;
    @Value("${vnpay.payUrl}")     private String vnpPayUrl;
    @Value("${vnpay.returnUrl}")  private String vnpReturnUrl;

    // ========= PUBLIC API =========

    @Transactional
    public ResPaymentCheckout createCheckout(String orderCode, PaymentMethod provider, String returnUrlOverride, String clientIp) {
        if (provider != PaymentMethod.VNPAY) {
            throw new UnsupportedOperationException("Provider not supported: " + provider);
        }

        Order order = orderRepo.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getGrandTotal() == null || order.getGrandTotal().signum() <= 0) {
            throw new IllegalStateException("Invalid amount");
        }

        // idempotent: 1 đơn -> 1 payment pending
        Payment payment = paymentRepo.findByOrder_Code(orderCode).orElseGet(() -> {
            Payment p = new Payment();
            p.setOrder(order);
            p.setProvider(provider);
            p.setStatus(PaymentStatus.PENDING);
            p.setAmount(order.getGrandTotal());
            p.setCurrency("VND");
            return paymentRepo.save(p);
        });

        String returnUrl = (returnUrlOverride != null && !returnUrlOverride.isBlank()) ? returnUrlOverride : vnpReturnUrl;
        String checkoutUrl = buildVnpayUrl(order, returnUrl, clientIp);
        payment.setCheckoutUrl(checkoutUrl);
        paymentRepo.save(payment);

        return new ResPaymentCheckout(order.getCode(), provider, checkoutUrl, null);
    }

    /** Handle IPN từ raw query string (giữ nguyên biểu diễn để ký) */
    @Transactional
    public Map<String, String> handleVnpayIpnRaw(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of("RspCode","99","Message","Empty query");
        }

        // 1) Parse + DECODE: chuyển '+' -> ' ' và %xx -> char
        Map<String, String> all = parseQueryDecoded(rawQuery);
        log.info("VNPAY IPN RAW  >>> {}", rawQuery);

        String recv = all.get("vnp_SecureHash");
        String type = all.getOrDefault("vnp_SecureHashType", "HmacSHA512");

        // 2) Lọc các tham số vnp_* (trừ hash) và build canonical (re-encode)
        Map<String, String> v = new TreeMap<>();
        all.forEach((k, val) -> {
            if (k.startsWith("vnp_") && !"vnp_SecureHash".equals(k) && !"vnp_SecureHashType".equals(k)) {
                v.put(k, val);
            }
        });
        String canonical = toQueryAscii(v); // encode bằng URLEncoder (US_ASCII)
        String calc = hmac512(vnpHashSecret, canonical).toUpperCase();

        log.info("VNPAY IPN CANONICAL >>> {}", canonical);
        log.info("VNPAY IPN HASH calc={} recv={}", calc, recv);

        if (recv == null || !calc.equalsIgnoreCase(recv)) {
            return Map.of("RspCode","97","Message","Fail checksum");
        }

        // 3) Business checks
        String code = v.get("vnp_TxnRef");
        String rsp  = v.get("vnp_ResponseCode");
        long amt100 = Long.parseLong(v.getOrDefault("vnp_Amount","0"));
        BigDecimal amount = BigDecimal.valueOf(amt100).divide(BigDecimal.valueOf(100));

        Order order = orderRepo.findByCode(code).orElse(null);
        if (order == null) return Map.of("RspCode","01","Message","Order not found");

        Payment payment = paymentRepo.findByOrder_Code(code).orElse(null);
        if (payment == null) return Map.of("RspCode","02","Message","Payment not found");

        if (!vnpTmnCode.equals(v.get("vnp_TmnCode"))) {
            return Map.of("RspCode","03","Message","Invalid TmnCode");
        }

        // idempotent
        if (payment.getStatus() == PaymentStatus.PAID) {
            return Map.of("RspCode","02","Message","Order already confirmed");
        }

        if (amount.compareTo(order.getGrandTotal()) != 0) {
            return Map.of("RspCode","04","Message","Invalid amount");
        }

        // 4) Update trạng thái
        if ("00".equals(rsp)) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setProviderTxnId(v.get("vnp_TransactionNo"));
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.PROCESSING);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        paymentRepo.save(payment);
        orderRepo.save(order);

        return Map.of("RspCode","00","Message","Confirm Success");
    }
    private static Map<String,String> parseQueryDecoded(String raw) {
        Map<String,String> m = new TreeMap<>();
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            String k = pair.substring(0, idx);
            String v = pair.substring(idx + 1);
            String dk = URLDecoder.decode(k, StandardCharsets.UTF_8);
            String dv = URLDecoder.decode(v, StandardCharsets.UTF_8);
            m.put(dk, dv);
        }
        return m;
    }


    /** Return URL (dùng cho FE hiển thị). Ở đây không thay đổi trạng thái đơn. */
    public Map<String, Object> handleVnpayReturnRaw(String rawQuery) {
        Map<String, String> p = parseQueryRaw(rawQuery);
        return Map.of(
                "orderCode", p.get("vnp_TxnRef"),
                "responseCode", p.get("vnp_ResponseCode"),
                "bankCode", p.get("vnp_BankCode")
        );
    }

    // ========= INTERNALS =========

    private String buildVnpayUrl(Order order, String returnUrl, String clientIp) {
        Map<String, String> v = new TreeMap<>();
        v.put("vnp_Version", "2.1.0");
        v.put("vnp_Command", "pay");
        v.put("vnp_TmnCode", vnpTmnCode);

        // amount * 100 → số nguyên, không thập phân
        String amt = order.getGrandTotal()
                .movePointRight(2) // *100
                .setScale(0)       // no decimals
                .toPlainString();
        v.put("vnp_Amount", amt);

        v.put("vnp_CurrCode", "VND");
        v.put("vnp_TxnRef", order.getCode()); // phải unique
        v.put("vnp_OrderInfo", "Thanh toan don hang " + order.getCode());
        v.put("vnp_OrderType", "other");
        v.put("vnp_Locale", "vn");
        v.put("vnp_IpAddr", (clientIp == null || clientIp.isBlank()) ? "127.0.0.1" : clientIp);

        v.put("vnp_CreateDate", nowVN());
        v.put("vnp_ExpireDate", plusMinutesVN(15));
        v.put("vnp_ReturnUrl", returnUrl);
        // KHÔNG gửi vnp_IpnUrl trong params

        String canonical = toQueryAscii(v);
        String hash = hmac512(vnpHashSecret, canonical).toUpperCase();

        log.info("VNPAY CREATE CANONICAL >>> {}", canonical);
        log.info("VNPAY CREATE HASH       >>> {}", hash);

        // thêm SecureHashType để rõ ràng
        return vnpPayUrl + "?" + canonical + "&vnp_SecureHashType=HmacSHA512&vnp_SecureHash=" + hash;
    }

    private static String nowVN() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(Instant.now());
    }

    private static String plusMinutesVN(int m) {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(Instant.now().plusSeconds(m * 60L));
    }

    /** Build canonical query theo sample VNPAY: sort key↑, URLEncoder US_ASCII */
    private static String toQueryAscii(Map<String,String> m) {
        StringBuilder sb = new StringBuilder();
        for (var e : m.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    /** HmacSHA512(hex lowercase) */
    private static String hmac512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] h = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Parse raw query "a=1&b=2%20x" -> Map giữ nguyên value (không decode) */
    private static Map<String,String> parseQueryRaw(String raw) {
        Map<String,String> m = new TreeMap<>();
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            String k = pair.substring(0, idx);
            String v = pair.substring(idx + 1); // giữ nguyên, có thể chứa % hoặc +
            m.put(k, v);
        }
        return m;
    }
}
