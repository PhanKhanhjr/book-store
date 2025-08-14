package phankhanh.book_store.service;

import org.springframework.stereotype.Service;
import phankhanh.book_store.Repository.UserRepository;
import phankhanh.book_store.domain.User;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
    public User getUserByUsername(String username) {
        return userRepository.findByEmail(username);
    }
}
