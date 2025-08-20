package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update user by ID")
    public ResponseEntity<ResUserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody ReqUserUpdate resUser) throws IdInvalidException {
        User updateUser = userService.updateUser(id, resUser);
        return ResponseEntity.ok(this.userService.convertToResUserDTO(updateUser));
    }

    @PutMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) throws IdInvalidException {
        userService.softDeleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /** Hard delete (chỉ dùng khi thật cần) */
    @DeleteMapping("users/{id}/hard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) throws IdInvalidException {
        userService.hardDeleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @DeleteMapping("users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) throws IdInvalidException {
        Long userId = jwt.getClaim("userId");
        userService.softDeleteSelf(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //debug endpoint to check authentication and roles
    @GetMapping("/debug/auth")
    public Map<String, Object> debug(Authentication auth, @AuthenticationPrincipal Jwt jwt) {
        return Map.of("authorities", auth.getAuthorities().toString(),
                "rolesClaim", jwt.getClaimAsStringList("roles"));
    }

}
