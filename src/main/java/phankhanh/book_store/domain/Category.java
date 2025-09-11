package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import phankhanh.book_store.util.SlugUtil;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories",
        indexes = @Index(name = "idx_category_slug", columnList = "slug", unique = true))
@Getter @Setter @NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void prePersist() {
        if (this.name != null && (this.slug == null || this.slug.isBlank())) {
            this.slug = SlugUtil.toSlug(this.name);
        }
    }

    public Category(String name) {
        this.name = name;
    }
}

