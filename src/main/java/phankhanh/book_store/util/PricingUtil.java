package phankhanh.book_store.util;

import lombok.RequiredArgsConstructor;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.CartItem;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.util.constant.DeliveryMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


public final class PricingUtil {
    private PricingUtil() {}
    public static BigDecimal effectivePrice(phankhanh.book_store.domain.Book b, Instant now) {
        if (b.getSalePrice() != null && b.getSaleStartAt() != null && b.getSaleEndAt() != null) {
            if (!now.isBefore(b.getSaleStartAt()) && !now.isAfter(b.getSaleEndAt())) {
                return b.getSalePrice();
            }
        }
        return b.getPrice();
    }

    public static BigDecimal unitDiscount(phankhanh.book_store.domain.CartItem ci) {
        return BigDecimal.ZERO;
    }
    public static BigDecimal discountTotal(List<CartItem> items) {
        return items.stream()
                .map(ci -> unitDiscount(ci).multiply(BigDecimal.valueOf(ci.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal tax(BigDecimal taxable) {
        return BigDecimal.ZERO; // để trống, sau này m thêm VAT % thì áp dụng ở đây
    }

    public static BigDecimal shippingFee(DeliveryMethod method,
                                         phankhanh.book_store.domain.Cart cart) {
        // gọn cho đồ án: nếu PICKUP thì free, còn lại 20k chẳng hạn
        return new BigDecimal("30000");
    }

}
