package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import phankhanh.book_store.util.constant.PaymentMethod;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id",
            foreignKey=@ForeignKey(name="fk_payment_order"))
    private Order order;

    @Enumerated(EnumType.STRING) private PaymentMethod provider;
    @Enumerated(EnumType.STRING) private PaymentStatus status;
    @Column(precision=14, scale=2, nullable=false) private BigDecimal amount;
    private String currency; // "VND"
    private String providerTxnId;
    @Column(length=500) private String checkoutUrl;
    @Column(columnDefinition="jsonb") private String extra;
    @CreationTimestamp
    @Column(updatable=false) private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
