package phankhanh.book_store.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    @NotBlank @Size(max = 100)
    private String fullName;
    @NotBlank @Size(max = 20)
    private String phone;
    @Column(nullable = false, length = 255)
    private String line1;
    @Column(nullable = false, length = 100)
    private String ward;
    @Column(nullable = false, length = 100)
    private String district;
    @Column(nullable = false, length = 100)
    private String province;
    @Column(nullable = false)
    private boolean isDefault = false;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private boolean deleted = false;
}
