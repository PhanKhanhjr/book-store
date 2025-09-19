package phankhanh.book_store.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(name = "uk_rating_book_user", columnNames = {"book_id","user_id"}),
        indexes = {
                @Index(name = "idx_rating_book", columnList = "book_id"),
                @Index(name = "idx_rating_user", columnList = "user_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "book_id",
            foreignKey = @ForeignKey(name = "fk_rating_book"), nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_rating_user"), nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private Instant deletedAt;

    @Column(length = 3000)
    private String content;
}
