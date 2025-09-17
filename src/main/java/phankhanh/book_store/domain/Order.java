package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import phankhanh.book_store.util.AddressSnapshot;
import phankhanh.book_store.util.constant.DeliveryMethod;
import phankhanh.book_store.util.constant.OrderStatus;
import phankhanh.book_store.util.constant.PaymentMethod;
import phankhanh.book_store.util.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_created_at", columnList = "user_id, created_at DESC")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_code", columnNames = "code")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String code; // ví dụ BK250912-000123

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // optional=false nếu bắt buộc có user
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_order_user"))
    private User user;

    //STATUS & PAYMENT
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PaymentStatus paymentStatus;

    //TỔNG TIỀN
    @Column(precision=14, scale=2, nullable=false) private BigDecimal subtotal;       // trước giảm/thuế/ship
    @Column(precision=14, scale=2, nullable=false) private BigDecimal discountTotal;  // tổng giảm giá
    @Column(precision=14, scale=2, nullable=false) private BigDecimal shippingFee;    // phí ship (m tự quy định)
    @Column(precision=14, scale=2, nullable=false) private BigDecimal taxTotal;       // thuế (nếu có)
    @Column(precision=14, scale=2, nullable=false) private BigDecimal grandTotal;     // phải trả cuối cùng

    // DELIVERY
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DeliveryMethod deliveryMethod; // STANDARD/EXPRESS/PICKUP...
    @Column(length=500) private String note;

    //SNAPSHOT ĐỊA CHỈ NHẬN HÀNG
    @Embedded
    private AddressSnapshot shipping;

    // Metadata thanh toán
    @Column(length = 10) private String currency;     // "VND"
    @Column(length = 40) private String paymentRef;   // mã nội bộ (nếu cần)
    private Instant paidAt;

    // TIMESTAMPS
    @CreationTimestamp @Column(name="created_at", updatable=false)
    private Instant createdAt;
    @UpdateTimestamp  @Column(name="updated_at")
    private Instant updatedAt;
    private Instant confirmedAt;
    private Instant shippedAt;
    private Instant completedAt;
    private Instant canceledAt;

    // ITEMS
    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<OrderItem> items = new ArrayList<>();

    //Assignee
    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "assignee_name", length = 100)
    private String assigneeName;
    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    // thời điểm hoàn tiền xong
    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "shipping_carrier", length = 100)
    private String shippingCarrier;

    @Column(name = "shipping_tracking_code", length = 100)
    private String shippingTrackingCode;

}
