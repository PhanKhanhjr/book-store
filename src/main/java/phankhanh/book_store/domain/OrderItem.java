package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id",
            foreignKey=@ForeignKey(name="fk_orderitem_order"))
    private Order order;
    private Long bookId;
    @Column(nullable=false, length=250) private String titleSnapshot;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal priceSnapshot;
    @Column(nullable=false) private Integer qty;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal lineTotal;
    @Column(length = 64)  private String skuSnapshot;
    @Column(length = 500) private String imageUrlSnapshot;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal discountSnapshot;
}