package phankhanh.book_store.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    // helper 2 chiều
    public void addUser(User u) {
        if (!users.contains(u)) {
            users.add(u);
            u.setRole(this);
        }
    }
    public void removeUser(User u) {
        if (users.remove(u) && u.getRole() == this) {
            u.setRole(null);
        }
    }
}
