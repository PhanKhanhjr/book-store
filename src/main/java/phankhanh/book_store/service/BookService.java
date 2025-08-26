package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.request.ReqBookCreate;
import phankhanh.book_store.DTO.request.ReqBookUpdate;
import phankhanh.book_store.DTO.response.ResBookDetailDTO;
import phankhanh.book_store.DTO.response.ResBookListItemDTO;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.Inventory;
import phankhanh.book_store.domain.Publisher;
import phankhanh.book_store.domain.Supplier;
import phankhanh.book_store.repository.*;
import phankhanh.book_store.util.BookMapper;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {
    private final BookRepository bookRepo;
    private final AuthorRepository authorRepo;
    private final CategoryRepository categoryRepo;
    private final PublisherRepository publisherRepo;
    private final SupplierRepository supplierRepo;

    public ResBookDetailDTO create(ReqBookCreate req) {
        Book b = new Book();

        Publisher publisher = publisherRepo.findById(req.publisherId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        Supplier supplier = supplierRepo.findById(req.supplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
        var authors = req.authorIds().stream()
                .map(id -> authorRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Author not found: " + id)))
                .collect(Collectors.toSet());
        var categories = req.categoryIds().stream()
                .map(id -> categoryRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id)))
                .collect(Collectors.toSet());

        BookMapper.mapCreateReqToEntity(req, b, publisher, supplier, authors, categories);

        if (b.getSlug() == null || b.getSlug().isBlank()) {
            b.setSlug(slugify(req.title()));
        }

        Inventory inv = Inventory.builder().book(b).stock(req.initialStock()).sold(0).build();
        b.setInventory(inv);

        return BookMapper.toDetail(bookRepo.save(b));
    }

    public ResBookDetailDTO update(Long id, ReqBookUpdate req) {
        Book b = bookRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Publisher publisher = publisherRepo.findById(req.publisherId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        Supplier supplier = supplierRepo.findById(req.supplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
        var authors = req.authorIds().stream()
                .map(i -> authorRepo.findById(i)
                        .orElseThrow(() -> new IllegalArgumentException("Author not found: " + i)))
                .collect(Collectors.toSet());
        var categories = req.categoryIds().stream()
                .map(i -> categoryRepo.findById(i)
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + i)))
                .collect(Collectors.toSet());

        b.getImages().clear();
        BookMapper.mapUpdateReqToEntity(req, b, publisher, supplier, authors, categories);

        return BookMapper.toDetail(bookRepo.save(b));
    }

    @Transactional
    public ResBookDetailDTO getDetail(String slug) {
        Book b = bookRepo.findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        return BookMapper.toDetail(b);
    }

    @Transactional
    public Page<ResBookListItemDTO> listAll(Pageable pageable) {
        return bookRepo.findByDeletedFalse(pageable).map(BookMapper::toListItem);
    }

    private String slugify(String input) {
        return input.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
    }
}

