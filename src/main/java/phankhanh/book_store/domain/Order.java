package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import phankhanh.book_store.util.constant.DeliveryMethod;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentMethod;
import phankhanh.book_store.util.constant.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String userId;

    @Enumerated(EnumType.STRING) private OrderStatus status;
    @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) private PaymentStatus paymentStatus;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal subtotal;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal discountTotal;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal shippingFee;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal taxTotal;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal grandTotal;
    @Enumerated(EnumType.STRING) private DeliveryMethod deliveryMethod;
    @Column(length=500) private String note;
    @CreationTimestamp
    @Column(updatable=false) private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    private Instant canceledAt; private Instant completedAt;
    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<OrderItem> items = new ArrayList<>();
}
