package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import phankhanh.book_store.util.SlugUtil;

@Entity
@Table(name = "suppliers")
@Getter @Setter @NoArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 200, unique = true)
    private String slug;

    @PrePersist @PreUpdate
    void prePersist() { this.slug = SlugUtil.toSlug(this.name); }

    public Supplier(String name) {
        this.name = name;
    }
}

