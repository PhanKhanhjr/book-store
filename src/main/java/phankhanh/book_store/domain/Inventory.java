package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_inventory_book"))
    private Book book;

    @Column(nullable = false)
    private Integer stock; // số lượng hiện có

    @Column(nullable = false)
    private Integer sold;  // đã bán (phục vụ hiển thị)
}
