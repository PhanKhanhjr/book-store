package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;

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

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}

