package phankhanh.book_store.util;

import phankhanh.book_store.util.constant.AgeRating;

import java.util.List;

public final class AgeRatingUtil {
    private AgeRatingUtil() {}

    private static final List<AgeRating> ORDERED = List.of(
            AgeRating.ALL, AgeRating._6PLUS, AgeRating._12PLUS, AgeRating._16PLUS, AgeRating._18PLUS
    );

    public static AgeRating fromMinYears(Integer years) {
        if (years == null) return null;
        if (years >= 18) return AgeRating._18PLUS;
        if (years >= 16) return AgeRating._16PLUS;
        if (years >= 12) return AgeRating._12PLUS;
        if (years >= 6)  return AgeRating._6PLUS;
        return AgeRating.ALL;
    }

    public static AgeRating fromMaxYears(Integer years) {
        if (years == null) return null;
        if (years < 6)   return AgeRating.ALL;
        if (years < 12)  return AgeRating._6PLUS;
        if (years < 16)  return AgeRating._12PLUS;
        if (years < 18)  return AgeRating._16PLUS;
        return AgeRating._18PLUS;
    }

    public static List<AgeRating> between(AgeRating min, AgeRating max) {
        if (min == null && max == null) return ORDERED;
        int i0 = (min == null) ? 0 : ORDERED.indexOf(min);
        int i1 = (max == null) ? ORDERED.size() - 1 : ORDERED.indexOf(max);
        if (i0 < 0 || i1 < 0) return ORDERED; // fallback
        if (i0 > i1) { int t = i0; i0 = i1; i1 = t; }
        return ORDERED.subList(i0, i1 + 1);
    }
}
