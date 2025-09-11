package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="cart_items",
        uniqueConstraints=@UniqueConstraint(name="uk_cart_book", columnNames={"cart_id","book_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cart_id",
            foreignKey=@ForeignKey(name="fk_cartitem_cart"))
    private Cart cart;

    @Column(nullable=false) private Long bookId;
    @Column(nullable=false) private Integer qty;
    @Column(nullable=false) private Boolean selected = false;

    @Column(precision=14, scale=2, nullable=false) private BigDecimal unitPriceCache = BigDecimal.ZERO;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal lineTotalCache = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(updatable=false) private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
