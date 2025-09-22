package phankhanh.book_store.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comment_likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_comment_like_comment_user",
                columnNames = {"comment_id","user_id"}),
        indexes = {
                @Index(name = "idx_comment_like_comment", columnList = "comment_id"),
                @Index(name = "idx_comment_like_user", columnList = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "comment_id",
            foreignKey = @ForeignKey(name = "fk_like_comment"), nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_like_user"), nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}