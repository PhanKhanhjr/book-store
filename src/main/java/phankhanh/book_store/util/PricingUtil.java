package phankhanh.book_store.util;

import java.math.BigDecimal;
import java.time.Instant;

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
}
