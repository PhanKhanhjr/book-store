package phankhanh.book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phankhanh.book_store.DTO.request.ReqBookCreate;
import phankhanh.book_store.DTO.request.ReqBookUpdate;
import phankhanh.book_store.DTO.response.ResBookDetailDTO;
import phankhanh.book_store.DTO.response.ResBookListItemDTO;
import phankhanh.book_store.DTO.response.ResHomeFeedDTO;
import phankhanh.book_store.domain.Author;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.Category;
import phankhanh.book_store.domain.Inventory;
import phankhanh.book_store.domain.Publisher;
import phankhanh.book_store.domain.Supplier;
import phankhanh.book_store.repository.AuthorRepository;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.CategoryRepository;
import phankhanh.book_store.repository.PublisherRepository;
import phankhanh.book_store.repository.SupplierRepository;
import phankhanh.book_store.util.AgeRatingUtil;
import phankhanh.book_store.util.BookMapper;
import phankhanh.book_store.util.BookSpecifications;
import phankhanh.book_store.util.constant.AgeRating;
import phankhanh.book_store.util.constant.Language;
import phankhanh.book_store.util.constant.ProductStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepo;
    private final PublisherRepository publisherRepo;
    private final SupplierRepository supplierRepo;
    private final AuthorRepository authorRepo;
    private final CategoryRepository categoryRepo;

    /* ================== QUERY ================== */

    /* ===== LIST (không search) ===== */
    @Transactional(readOnly = true)
    public Page<ResBookListItemDTO> list(ProductStatus status,
                                         Long authorId, Long categoryId,
                                         Long publisherId, Long supplierId,
                                         Pageable pageable) {
        var page = bookRepo.searchBooks(
                null, authorId, categoryId, publisherId, supplierId, status, pageable
        );
        return page.map(BookMapper::toListItem);
    }

    /* ===== SEARCH ===== */
    @Transactional(readOnly = true)
    public Page<ResBookListItemDTO> search(String q, ProductStatus status,
                                           Long authorId, Long categoryId,
                                           Long publisherId, Long supplierId,
                                           Pageable pageable) {
        var page = bookRepo.searchBooks(
                q, authorId, categoryId, publisherId, supplierId, status, pageable
        );
        return page.map(BookMapper::toListItem);
    }

    /* ===== HOME FEED ===== */
    @Transactional(readOnly = true)
    public ResHomeFeedDTO buildHomeFeed(ProductStatus status,
                                        int featuredSize, int newSize, int bestSize,
                                        List<Long> categoryIds, int perCategory) {
        var featured = bookRepo.findOnSale(status, featuredSize, java.time.Instant.now())
                .stream().map(BookMapper::toListItem).toList();

        var newest = bookRepo.findNewest(status, newSize)
                .stream().map(BookMapper::toListItem).toList();

        var best    = bookRepo.findBestSellers(status, bestSize)
                .stream().map(BookMapper::toListItem).toList();

        List<ResHomeFeedDTO.CategorySection> sections = java.util.Collections.emptyList();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            sections = categoryIds.stream().map(cid -> {
                var cat = categoryRepo.findById(cid).orElse(null);
                String name = (cat != null) ? cat.getName() : ("Category #" + cid);
                var items = bookRepo.findTopByCategory(cid, status, perCategory)
                        .stream().map(BookMapper::toListItem).toList();
                return new ResHomeFeedDTO.CategorySection(cid, name, items);
            }).toList();
        }

        return new ResHomeFeedDTO(featured, newest, best, sections);
    }

    @Transactional(readOnly = true)
    public ResBookDetailDTO getById(Long id) {
        Book b = bookRepo.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        return BookMapper.toDetail(b);
    }

    @Transactional(readOnly = true)
    public ResBookDetailDTO getBySlug(String slug) {
        Book b = bookRepo.findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return BookMapper.toDetail(b);
    }

    /* ================== COMMAND ================== */

    @Transactional
    public ResBookDetailDTO create(ReqBookCreate req) {
        Book b = BookMapper.toEntity(req);

        // Gắn quan hệ theo id từ req (nếu có)
        if (req.publisherId() != null) {
            Publisher pub = publisherRepo.findById(req.publisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found"));
            b.setPublisher(pub);
        }
        if (req.supplierId() != null) {
            Supplier sup = supplierRepo.findById(req.supplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            b.setSupplier(sup);
        }
        if (req.authorIds() != null && !req.authorIds().isEmpty()) {
            List<Author> authors = authorRepo.findAllById(req.authorIds());
            if (authors.size() != req.authorIds().size()) {
                throw new RuntimeException("Some authors not found");
            }
            b.setAuthors(new HashSet<>(authors));
        }
        if (req.categoryIds() != null && !req.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepo.findAllById(req.categoryIds());
            if (categories.size() != req.categoryIds().size()) {
                throw new RuntimeException("Some categories not found");
            }
            b.setCategories(new HashSet<>(categories));
        }

        // (Tuỳ chọn) tạo inventory mặc định nếu muốn
        int init = (req.initialStock() == null || req.initialStock() < 0 ) ? 0 : req.initialStock();
        Inventory inv = b.getInventory();
        if (inv == null) {
            inv = new Inventory();
            inv.setBook(b);      // owning side
            inv.setStock(init);
            inv.setSold(0);
            b.setInventory(inv); // inverse side (có cascade)
        } else {
            inv.setStock(init);
            if (inv.getSold() == null) inv.setSold(0);
        }

        try {
            bookRepo.save(b);
        } catch (DataIntegrityViolationException ex) {
            // bắt case trùng slug/isbn13 (unique index)
            throw new RuntimeException("Duplicate slug or ISBN-13", ex);
        }
        return BookMapper.toDetail(b);
    }

    @Transactional
    public ResBookDetailDTO update(Long id, ReqBookUpdate req) {
        Book b = bookRepo.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));

        // Map field primitive/enums/images
        BookMapper.updateEntity(b, req);

        // Cập nhật quan hệ nếu request có gửi id (nếu null => giữ nguyên)
        if (req.publisherId() != null) {
            Publisher pub = publisherRepo.findById(req.publisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found"));
            b.setPublisher(pub);
        }
        if (req.supplierId() != null) {
            Supplier sup = supplierRepo.findById(req.supplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            b.setSupplier(sup);
        }
        if (req.authorIds() != null) {
            Set<Author> authors = new HashSet<>(authorRepo.findAllById(req.authorIds()));
            if (authors.size() != req.authorIds().size()) {
                throw new RuntimeException("Some authors not found");
            }
            b.setAuthors(authors);
        }
        if (req.categoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepo.findAllById(req.categoryIds()));
            if (categories.size() != req.categoryIds().size()) {
                throw new RuntimeException("Some categories not found");
            }
            b.setCategories(categories);
        }

        try {
            // không cần save tường minh nếu entity đang managed, nhưng gọi save cũng ok
            bookRepo.save(b);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Duplicate slug or ISBN-13", ex);
        }
        return BookMapper.toDetail(b);
    }

    @Transactional
    public void delete(Long id) {
        Book b = bookRepo.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        b.setDeleted(true); // soft delete
        // save không bắt buộc nếu managed, nhưng an toàn thì gọi:
        bookRepo.save(b);
    }

    public Page<Book> filterBooks(
            String category, String publisher, String supplier,
            Language language, ProductStatus status,
            BigDecimal priceMin, BigDecimal priceMax,
            AgeRating ageMin, AgeRating ageMax,    // nhận theo enum
            Integer ageMinYears, Integer ageMaxYears,
            Pageable pageable) {
        AgeRating minBucket = (ageMin != null) ? ageMin : AgeRatingUtil.fromMinYears(ageMinYears);
        AgeRating maxBucket = (ageMax != null) ? ageMax : AgeRatingUtil.fromMaxYears(ageMaxYears);
        List<Long> categoryIds = null;
        if (category != null && !category.isBlank()) {
            categoryIds = categoryRepo.findDescendantIdsBySlug(category);
            if (categoryIds.isEmpty()) return Page.empty(pageable);
        }

        Specification<Book> spec = BookSpecifications.notDeleted()
                    .and(BookSpecifications.hasAnyCategoryIds(categoryIds))
//                    .and(BookSpecifications.hasCategory(category))
                    .and(BookSpecifications.hasPublisher(publisher))
                    .and(BookSpecifications.hasSupplier(supplier))
                    .and(BookSpecifications.hasLanguage(language))
                    .and(BookSpecifications.hasStatus(status))
                    .and(BookSpecifications.priceBetween(priceMin, priceMax))
                    .and(BookSpecifications.priceBetween(priceMin, priceMax))
                    .and(BookSpecifications.ageInRange(minBucket, maxBucket));
            return bookRepo.findAll(spec, pageable);
        }
    }
