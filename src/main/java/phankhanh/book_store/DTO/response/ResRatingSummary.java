package phankhanh.book_store.DTO.response;

import java.math.BigDecimal;
import java.util.Map;
public record ResRatingSummary(
        BigDecimal average, int count,
        Map<Integer,Integer> distribution,
        Map<Integer,BigDecimal> percent
) {}
