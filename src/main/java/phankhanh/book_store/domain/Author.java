package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import phankhanh.book_store.util.SlugUtil;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
@Getter @Setter @NoArgsConstructor
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;
    @ManyToMany(mappedBy = "authors")
    private Set<Book> books = new HashSet<>();

    @Column(nullable = false, length = 200, unique = true)
    private String slug;

    @PrePersist @PreUpdate
    void prePersist() { this.slug = SlugUtil.toSlug(this.name); }


    public Author(String name) {
        this.name = name;
    }
}

