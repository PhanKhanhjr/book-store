package phankhanh.book_store.util;

public final class SlugUtil {
    private SlugUtil() {}

    public static String toSlug(String input) {
        if (input == null) return null;
        String noAccent = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String ascii = noAccent.replace('đ','d').replace('Đ','D');
        String slug = ascii.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? null : slug;
    }
}

