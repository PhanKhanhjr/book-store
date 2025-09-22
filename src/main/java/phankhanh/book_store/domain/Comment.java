package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import phankhanh.book_store.util.constant.CommentStatus;

import java.time.Instant;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_book", columnList = "book_id"),
        @Index(name = "idx_comment_parent", columnList = "parent_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id",
            foreignKey = @ForeignKey(name = "fk_comment_book"),
            nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_comment_user"),
            nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // nếu là reply thì có parent
    @JoinColumn(name = "parent_id",
            foreignKey = @ForeignKey(name = "fk_comment_parent"))
    private Comment parent;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant updatedAt;

    // soft delete;
    private Instant deletedAt;
    private Long deletedBy; // admin or owner id
}
