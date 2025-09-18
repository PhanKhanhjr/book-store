package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import phankhanh.book_store.util.constant.RefundMethod;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SALE','ADMIN')")
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    @GetMapping
    public Page<ResOrderAdmin> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable
    ) {
        // clamp size
        int size = Math.min(pageable.getPageSize(), 100);
        int page = pageable.getPageNumber();
        Sort sort = pageable.getSort();

        PageRequest p = PageRequest.of(page, size, sort);

        return saleOrderService.searchForSale(q, status, paymentStatus, from, to, p);
    }

    @GetMapping("/{id}")
    public ResOrderAdmin detail(@PathVariable Long id) {
        return saleOrderService.getAdminView(id);
    }

    @PutMapping("/{id}/status")
    public ResOrderAdmin updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody ReqUpdateOrderStatus req,
                                      @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        return saleOrderService.updateStatusBySale(id, req, actorId);
    }

    @PutMapping("/{id}/payment")
    public ResOrderAdmin updatePayment(@PathVariable Long id,
                                       @Valid @RequestBody ReqUpdatePayment req,
                                       @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        return saleOrderService.updatePaymentBySale(id, req, actorId);
    }

    @PutMapping("/{id}/shipping")
    public ResOrderAdmin updateShipping(@PathVariable Long id,
                                        @Valid @RequestBody ReqUpdateShipping req,
                                        @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        return saleOrderService.updateShippingBySale(id, req, actorId);
    }

    @PutMapping("/{id}/assign")
    public ResOrderAdmin assign(@PathVariable Long id,
                                @Valid @RequestBody ReqAssignOrder req,
                                @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        return saleOrderService.assignOrder(id, req.assigneeId(), actorId);
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void addNote(@PathVariable Long id,
                        @Valid @RequestBody ReqCreateNote req,
                        @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        saleOrderService.addInternalNote(id, actorId, req.note());
    }

    // Đổi DELETE+body -> POST cho an toàn
    @PostMapping("/{id}/cancel")
    public ResOrderAdmin cancel(@PathVariable Long id,
                                @Valid @RequestBody ReqCancelOrder req,
                                @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        return saleOrderService.cancelBySale(id, req.reason(), actorId);
    }

    @PostMapping("/{id}/refund-manual")
    public ResOrderAdmin refundManual(@PathVariable Long id,
                                      @Valid @RequestBody ReqRefundManual req,
                                      @AuthenticationPrincipal(expression = "claims['userId']") Long actorId) {
        return saleOrderService.refundManual(id, req.amount(), req.method(), actorId);
    }

    public record ReqRefundManual(
            @NotNull BigDecimal amount,
            @NotNull RefundMethod method
    ) {}


    // ===== Inline DTOs =====
    public record ReqAssignOrder(@NotNull Long assigneeId) {}
    public record ReqCreateNote(@NotBlank String note) {}
    public record ReqCancelOrder(@NotBlank String reason) {}
    public record ReqRefund(
            @NotNull @jakarta.validation.constraints.DecimalMin("0.01") BigDecimal amount,
            @jakarta.validation.constraints.Size(max = 255) String reason
    ) {}
}
