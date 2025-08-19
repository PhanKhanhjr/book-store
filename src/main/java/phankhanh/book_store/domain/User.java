package phankhanh.book_store.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Component;
import phankhanh.book_store.util.SecurityUtil;
import phankhanh.book_store.util.constant.GenderEnum;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String fullName;
    private String username;
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    private String password;
    private GenderEnum gender;
    private LocalDate birthDate;
    private String phone;
    @Column(nullable = false)
    boolean enabled = false;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Instant deletedAt;
    @Column(name = "email_active", insertable = false, updatable = false, length = 255)
    private String emailActive;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @ToString.Exclude
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private java.util.List<Address> addresses = new java.util.ArrayList<>();


    // helper methods (tiện thêm/xoá và đồng bộ 2 chiều)
    public void addAddress(Address addr) {
        addresses.add(addr);
        addr.setUser(this);
    }
    public void removeAddress(Address addr) {
        addresses.remove(addr);
        addr.setUser(null);
    }

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }
    public void markDeleted() {
        this.enabled = false;
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
        }
    }
}
