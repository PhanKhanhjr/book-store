package phankhanh.book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phankhanh.book_store.DTO.request.ReqAddItem;
import phankhanh.book_store.DTO.request.ReqUpdateItem;
import phankhanh.book_store.DTO.response.ResCartItem;
import phankhanh.book_store.DTO.response.ResCartSummary;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.BookImage;
import phankhanh.book_store.domain.Cart;
import phankhanh.book_store.domain.CartItem;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.CartItemRepository;
import phankhanh.book_store.repository.CartRepository;
import phankhanh.book_store.repository.InventoryRepository;
import phankhanh.book_store.util.PricingUtil;
import phankhanh.book_store.util.constant.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    private static final int MAX_PER_LINE = 99;
    private static final String THUMB_SUFFIX = "_160x240";
    private static final java.util.regex.Pattern NAME_EXT_WITH_OPT_SIZE =
            java.util.regex.Pattern.compile("(?i)(.+?)(?:_\\d+x\\d+)?(\\.(webp|jpe?g|png))$");

    private String withSizeSuffix(String url, String suffix) {
        if (url == null) return null;
        int q = url.indexOf('?');
        String base = (q >= 0) ? url.substring(0, q) : url;
        String query = (q >= 0) ? url.substring(q) : "";

        var m = NAME_EXT_WITH_OPT_SIZE.matcher(base);
        if (m.matches()) {
            return m.group(1) + suffix + m.group(2) + query;
        }
        // không match (lạ hiếm gặp) ⇒ trả nguyên
        return url;
    }

    // lấy ảnh đầu (sortOrder nhỏ nhất) rồi thêm _160x240
    private String pickThumbnail(Book book) {
        if (book.getImages() == null || book.getImages().isEmpty()) return null;
        String original = book.getImages().stream()
                .sorted(java.util.Comparator.comparing(img -> img.getSortOrder() == null ? Integer.MAX_VALUE : img.getSortOrder()))
                .map(BookImage::getUrl)
                .findFirst().orElse(null);
        return withSizeSuffix(original, THUMB_SUFFIX);
    }

    @Transactional
    public ResCartSummary addItem(Long userId, ReqAddItem req) {
        if(req.qty() == null || req.qty() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
        var book = bookRepository.findById(req.bookId()).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if(book.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Book is not available");
        }

        var inventory = inventoryRepository.findByBook_Id(book.getId()).orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        int available = inventory.getStock();
        var now = Instant.now();
        BigDecimal unit = PricingUtil.effectivePrice(book, now);

        CartItem item = cartItemRepository.findByCart_IdAndBookId(cart.getId(), req.bookId()).orElse(null);
        int newQty = Math.min(MAX_PER_LINE, (item == null ? 0 : item.getQty()) + req.qty());
        if (newQty > available) newQty = available;
        if (newQty <= 0) throw new IllegalStateException("Out of stock");


        if (item == null) {
            item = CartItem.builder()
                    .cart(cart).bookId(req.bookId()).qty(newQty)
                    .selected(false)
                    .unitPriceCache(unit)
                    .lineTotalCache(unit.multiply(BigDecimal.valueOf(newQty)))
                    .build();
        } else {
            item.setQty(newQty);
            item.setUnitPriceCache(unit);
            item.setLineTotalCache(unit.multiply(BigDecimal.valueOf(newQty)));
        }
        cartItemRepository.save(item);

        return getCart(userId);
    }

    @Transactional
    public ResCartSummary removeItem(Long userId, Long bookId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Cart not found"));
        cartItemRepository.findByCart_IdAndBookId(cart.getId(), bookId).ifPresent(cartItemRepository::delete);
        return getCart(userId);
    }

    @Transactional
    public ResCartSummary clear(Long userId, boolean onlyUnselected) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Cart not found"));
        var items = cartItemRepository.findByCart_Id(cart.getId());
        for (var it : items) {
            if (!onlyUnselected || Boolean.FALSE.equals(it.getSelected())) cartItemRepository.delete(it);
        }
        return getCart(userId);
    }

    @Transactional
    public ResCartSummary updateItem(Long userId, Long bookId, ReqUpdateItem req) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        CartItem item = cartItemRepository.findByCart_IdAndBookId(cart.getId(), bookId).orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));
        if(req.qty() != null) {
            if(req.qty() <= 0){cartItemRepository.delete(item); return getCart(userId);}
            var inv = inventoryRepository.findByBook_Id(bookId).orElseThrow();
            int qty = Math.min(MAX_PER_LINE, Math.min(req.qty(), inv.getStock()));
            var book = bookRepository.findById(bookId).orElseThrow();
            BigDecimal unit = PricingUtil.effectivePrice(book, Instant.now());
            item.setQty(qty);
            item.setUnitPriceCache(unit);
            item.setLineTotalCache(unit.multiply(BigDecimal.valueOf(qty)));
        }
        if (req.selected() != null) item.setSelected(req.selected());
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional(readOnly = true)
    public ResCartSummary getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build());
        var items = (cart.getId() == null)
                ? java.util.List.<CartItem>of()
                : cartItemRepository.findByCart_Id(cart.getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0, totalSelected = 0;

        java.util.List<ResCartItem> resItems = new java.util.ArrayList<>();
        var now = java.time.Instant.now();

        for (var it : items) {
            var book = bookRepository.findById(it.getBookId()).orElse(null);
            if (book == null) continue;

            // giá gốc & giá hiệu lực (đã tính sale window)
            BigDecimal original = book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO;
            BigDecimal unit = PricingUtil.effectivePrice(book, now);
            if (original.signum() > 0 && unit.compareTo(original) > 0) unit = original; // clamp
            boolean onSale = original.signum() > 0 && unit.compareTo(original) < 0;

            BigDecimal line = unit.multiply(java.math.BigDecimal.valueOf(it.getQty()));

            // tồn kho hiện có
            var inv = inventoryRepository.findByBook_Id(it.getBookId()).orElse(null);
            int available = inv == null ? 0 : inv.getStock();

            // thumbnail: lấy ảnh sortOrder nhỏ nhất (0 là ảnh chính)
            String thumb = pickThumbnail(book);

            // item
            resItems.add(new ResCartItem(
                    it.getBookId(),
                    book.getTitle(),
                    book.getSlug(),
                    thumb,
                    onSale,
                    it.getQty(),
                    it.getSelected(),
                    original,
                    unit,
                    line,
                    available
            ));

            // cộng tổng (chỉ tính selected)
            if (Boolean.TRUE.equals(it.getSelected())) {
                subtotal = subtotal.add(line);
                totalSelected += it.getQty();
            }
            totalItems += it.getQty();
        }

        BigDecimal discount = BigDecimal.ZERO; // mã giảm giá tính ở /checkout/preview
        BigDecimal shipping = BigDecimal.ZERO; // tính ở preview
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal grand = subtotal.subtract(discount).add(shipping).add(tax);

        return new ResCartSummary(resItems, subtotal, discount, shipping, tax, grand, totalItems, totalSelected);
    }

    @Transactional
    public ResCartSummary selectAll(Long userId, boolean selected) {
        var cart = cartRepository.findByUserId(userId).orElse(null);
        if(cart != null) {
            var items = cartItemRepository.findByCart_Id(cart.getId());
            if(!items.isEmpty()) {
                for(var it: items) it.setSelected(selected);
                cartItemRepository.saveAll(items);
            }
        }
        return getCart(userId);
    }
}
