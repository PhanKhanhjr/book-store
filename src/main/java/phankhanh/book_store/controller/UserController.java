package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqUserUpdate;
import phankhanh.book_store.DTO.response.ResUserDTO;
import phankhanh.book_store.DTO.response.RestResponse;
import phankhanh.book_store.DTO.response.ResultPaginationDTO;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.service.UserDetailsCustom;
import phankhanh.book_store.service.UserService;
import phankhanh.book_store.util.CustomUserDetails;
import phankhanh.book_store.util.anotation.ApiMessage;
import phankhanh.book_store.util.error.IdInvalidException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
//    @GetMapping("/users")
//    @ApiMessage("Fetch all Users")
//    public ResponseEntity<ResultPaginationDTO<List<ResUserDTO>>> list(
//            @RequestParam(defaultValue = "0")  int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//            @RequestParam(defaultValue = "DESC")     String sortDir
//    ) {
//        Sort sort = sortDir.equalsIgnoreCase("ASC")
//                ? Sort.by(sortBy).ascending()
//                : Sort.by(sortBy).descending();
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // Page<User> -> Page<ResUserDTO>
//        Page<User> pageEntity = userService.fetchAllUser(pageable);
//        List<ResUserDTO> items = pageEntity.getContent()
//                .stream()
//                .map(
//                        user -> this.userService.convertToResUserDTO(user)
//                )
//                .toList();
//
//        Page<ResUserDTO> pageDto = new PageImpl<>(items, pageable, pageEntity.getTotalElements());
//
//        return ResponseEntity.ok(ResultPaginationDTO.of(pageDto));
//    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO<List<ResUserDTO>>> list(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        var pageEntity = userService.fetchAllUser(pageable);
        var items = pageEntity.getContent().stream().map(userService::convertToResUserDTO).toList();
        var pageDto = new PageImpl<>(items, pageable, pageEntity.getTotalElements());
        return ResponseEntity.ok( ResultPaginationDTO.of(pageDto));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Fetch user by ID")
    public ResponseEntity<ResUserDTO> findById(@PathVariable Long id) throws IdInvalidException {
        User user = userService.findUserById(id);
        ResUserDTO resUser = this.userService.convertToResUserDTO(user);
        return ResponseEntity.ok(resUser);
    }

    @PutMapping("/users/{id:\\d+}")
    @ApiMessage("Update user by ID")
    public ResponseEntity<ResUserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody ReqUserUpdate resUser) throws IdInvalidException {
        User updateUser = userService.updateUser(id, resUser);
        return ResponseEntity.ok(this.userService.convertToResUserDTO(updateUser));
    }

    @PutMapping("/users/me")
    public ResponseEntity<ResUserDTO> updateMe(
            @Valid @RequestBody ReqUserUpdate req,
            @AuthenticationPrincipal Jwt jwt
    ) throws IdInvalidException {
        Long userId = jwt.getClaim("userId");
        User updated = userService.updateUser(userId, req);
        ResUserDTO dto = userService.convertToResUserDTO(updated);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/users/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> softDelete(@PathVariable Long id) throws IdInvalidException {
        userService.softDeleteById(id);
        return ResponseEntity.ok("Delete success"); // 204
    }

    /** Hard delete (chỉ dùng khi thật cần) */
    @DeleteMapping("users/{id}/hard")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> hardDelete(@PathVariable Long id) throws IdInvalidException {
        userService.hardDeleteById(id);
        return ResponseEntity.ok("Hard delete success"); // 204
    }

    @DeleteMapping("/me")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteMe(@AuthenticationPrincipal Jwt jwt) throws IdInvalidException {
        Long userId = jwt.getClaim("userId"); // claim đã có trong token
        userService.softDeleteSelf(userId);
        return ResponseEntity.ok("Delete Success"); // 204
    }
}
