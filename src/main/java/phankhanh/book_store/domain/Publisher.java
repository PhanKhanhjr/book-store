package phankhanh.book_store.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "publishers")
@Getter @Setter @NoArgsConstructor
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    public Publisher(String name) {
        this.name = name;
    }
}


