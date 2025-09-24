package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_movement_inventory"))
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;   // IN, OUT, ADJUST

    @Column(nullable = false)
    private Integer delta;

    @Column(nullable = false)
    private Integer quantityAfter;

    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = java.time.Instant.now();

    public enum MovementType { IN, OUT, ADJUST }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
