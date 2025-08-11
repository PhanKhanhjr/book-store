package phankhanh.book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.service.UserService;
import phankhanh.book_store.util.error.InvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) throws InvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }
}
