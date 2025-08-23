package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import phankhanh.book_store.util.constant.AgeRating;
import phankhanh.book_store.util.constant.CoverType;
import phankhanh.book_store.util.constant.Language;
import phankhanh.book_store.util.constant.ProductStatus;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "books",
        indexes = {
                @Index(name = "idx_book_slug", columnList = "slug", unique = true),
                @Index(name = "idx_book_isbn13", columnList = "isbn13", unique = true)
        })
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    @Column(length = 32)
    private String sku;                // mã nội bộ

    @Column(length = 20)
    private String isbn13;             // mã vạch/ISBN-13

    @Lob
    private String description;        // mô tả dài (HTML/markdown tùy m)

    // Xuất bản
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id",
            foreignKey = @ForeignKey(name = "fk_book_publisher"))
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id",
            foreignKey = @ForeignKey(name = "fk_book_supplier"))
    private Supplier supplier;

    @ManyToMany
    @JoinTable(name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    @Builder.Default
    private Set<Author> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    // Thuộc tính vật lý
    private Integer pageCount;
    private Integer publicationYear;
    @Enumerated(EnumType.STRING)
    private Language language;
    private Integer weightGram;
    private Double widthCm;
    private Double heightCm;
    private Double thicknessCm;
    @Enumerated(EnumType.STRING)
    private CoverType coverType;
    @Enumerated(EnumType.STRING)
    private AgeRating ageRating;

    // Thương mại
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(nullable = false)
    private Long price;                 // giá gốc (VND)

    private Long salePrice;             // giá khuyến mãi (nullable)
    private Instant saleStartAt;
    private Instant saleEndAt;

    // Quan hệ phụ
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Inventory inventory;

    // Audit
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;
}
