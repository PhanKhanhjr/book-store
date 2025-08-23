package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.request.ReqUserUpdate;
import phankhanh.book_store.DTO.response.ResUserDTO;
import phankhanh.book_store.repository.UserRepository;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.util.constant.GenderEnum;
import phankhanh.book_store.util.error.DataInvalid;
import phankhanh.book_store.util.error.IdInvalidException;

import java.time.Instant;

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
        return userRepository.findByEmail(username).orElseThrow(() -> new DataInvalid("User not found with username: " + username));
    }
    public User findUserById(Long id) throws IdInvalidException {
        return userRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("User not found"));
    }

    public ResUserDTO convertToResUserDTO(User user) {
        return new ResUserDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getBirthDate(),
                user.getGender() == null ? null : user.getGender().name(),
                user.getRole()== null ? null : user.getRole().getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
    public Page<User> fetchAllUser(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User updateUser(Long id, ReqUserUpdate userUpdate) throws IdInvalidException, DataInvalid {
        User databaseUser = findUserById(id);
        if (userUpdate.fullName() != null) databaseUser.setFullName(userUpdate.fullName());
        if (userUpdate.phone() != null) databaseUser.setPhone(userUpdate.phone());
        if (userUpdate.birthDate() != null) databaseUser.setBirthDate(userUpdate.birthDate());
        if (userUpdate.gender() != null) {
            try {
                databaseUser.setGender(GenderEnum.valueOf(userUpdate.gender()));
            } catch (IllegalArgumentException e) {
                throw new DataInvalid("Invalid gender (FEMALE| MALE| OTHER)");
            }
        }
        return databaseUser;
    }

    public void softDeleteById(Long id) throws IdInvalidException {
        int updated = userRepository.softDelete(id, Instant.now());
        if (updated == 0) {
            throw new IdInvalidException("User not found or already deleted");
        }
    }
    public void softDeleteSelf(Long selfId) throws IdInvalidException {
        User u = userRepository.findById(selfId)
                .orElseThrow(() -> new IdInvalidException("User not found"));
//        if (u.getDeletedAt() == null) {
//            u.markDeleted();
//        }
        if(u.getDeletedAt() == null) {
            u.setDeletedAt(Instant.now());
            u.setEnabled(false);
            userRepository.save(u);
        }
    }
    public void hardDeleteById(Long id) throws IdInvalidException {
        boolean exists = userRepository.existsById(id);
        if (!exists) throw new IdInvalidException("User not found");
        userRepository.deleteById(id);
    }
}
