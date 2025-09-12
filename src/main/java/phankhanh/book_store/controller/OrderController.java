package phankhanh.book_store.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqCreateOrder;
import phankhanh.book_store.DTO.response.ResOrderCreated;
import phankhanh.book_store.DTO.response.ResOrderDetail;
import phankhanh.book_store.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** Tạo đơn từ giỏ hàng hiện tại của user */
    @PostMapping
    public ResOrderCreated create(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody ReqCreateOrder req) {
        Long userId = extractUserId(jwt);
        return orderService.createOrder(userId, req);
    }

    /** Lấy chi tiết đơn theo mã (user hoặc admin xem) */
    @GetMapping("/{code}")
    public ResOrderDetail getByCode(@PathVariable String code,
                                    @AuthenticationPrincipal Jwt jwt) {
        Long userId = extractUserId(jwt);
        // tuỳ chính sách: user chỉ xem đơn của chính họ, admin xem tất cả
        return orderService.getOrderDetailForUser(code, userId);
    }

    /** Danh sách đơn của user (phân trang) */
    @GetMapping("/me")
    public Page<ResOrderDetail> myOrders(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserId(jwt);
        return orderService.listMyOrders(userId, PageRequest.of(page, size));
    }

    // --- helper ---
    private Long extractUserId(Jwt jwt) {
        Object claim = jwt.getClaim("userId");
        if (claim instanceof Integer i) return i.longValue();
        if (claim instanceof Long l) return l;
        if (claim instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("Invalid userId claim");
    }
}

