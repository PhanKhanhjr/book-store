package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id",
            foreignKey = @ForeignKey(name = "fk_bookimage_book"))
    private Book book;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private Integer sortOrder; // 0 = ảnh chính
}
