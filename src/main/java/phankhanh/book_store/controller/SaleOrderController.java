package phankhanh.book_store.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import phankhanh.book_store.DTO.request.sale.ReqUpdateOrderStatus;
import phankhanh.book_store.DTO.request.sale.ReqUpdatePayment;
import phankhanh.book_store.DTO.request.sale.ReqUpdateShipping;
import phankhanh.book_store.DTO.response.sale.ResOrderAdmin;
import phankhanh.book_store.service.SaleOrderService;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SALE','ADMIN')")
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    // LIST + FILTER
    @GetMapping
    public Page<ResOrderAdmin> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable
    ) {
        return saleOrderService.searchForSale(q, status, paymentStatus, from, to, pageable);
    }

    // DETAIL
    @GetMapping("/{id}")
    public ResOrderAdmin detail(@PathVariable Long id) {
        return saleOrderService.getAdminView(id);
    }

    // UPDATE STATUS
    @PutMapping("/{id}/status")
    public ResOrderAdmin updateStatus(@PathVariable Long id,
                                      @RequestBody ReqUpdateOrderStatus req,
                                      @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        return saleOrderService.updateStatusBySale(id, req, actorId);
    }

    // UPDATE PAYMENT
    @PutMapping("/{id}/payment")
    public ResOrderAdmin updatePayment(@PathVariable Long id,
                                       @RequestBody ReqUpdatePayment req,
                                       @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        return saleOrderService.updatePaymentBySale(id, req, actorId);
    }

    // UPDATE SHIPPING
    @PutMapping("/{id}/shipping")
    public ResOrderAdmin updateShipping(@PathVariable Long id,
                                        @RequestBody ReqUpdateShipping req,
                                        @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        return saleOrderService.updateShippingBySale(id, req, actorId);
    }

    // ASSIGN ORDER
    @PutMapping("/{id}/assign")
    public ResOrderAdmin assign(@PathVariable Long id,
                                @RequestBody ReqAssignOrder req,
                                @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        return saleOrderService.assignOrder(id, req.assigneeId(), actorId);
    }

    // INTERNAL NOTE
    @PostMapping("/{id}/notes")
    public void addNote(@PathVariable Long id,
                        @RequestBody ReqCreateNote req,
                        @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        saleOrderService.addInternalNote(id, actorId, req.note());
    }

    // CANCEL ORDER
    @DeleteMapping("/{id}/cancel")
    public ResOrderAdmin cancel(@PathVariable Long id,
                                @RequestBody ReqCancelOrder req,
                                @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        return saleOrderService.cancelBySale(id, req.reason(), actorId);
    }

    // REFUND
    @PostMapping("/{id}/refund")
    public ResOrderAdmin refund(@PathVariable Long id,
                                @RequestBody ReqRefund req,
                                @AuthenticationPrincipal Jwt jwt) {
        Long actorId = jwt.getClaim("userId");
        return saleOrderService.refundBySale(id, req.amount(), req.reason(), actorId);
    }

    // ===== Inline DTOs (di chuyển sang package DTO.request.sale nếu muốn) =====
    public record ReqAssignOrder(@NotNull Long assigneeId) {}
    public record ReqCreateNote(@NotBlank String note) {}
    public record ReqCancelOrder(@NotBlank String reason) {}
    public record ReqRefund(@NotNull BigDecimal amount, String reason) {}
}
