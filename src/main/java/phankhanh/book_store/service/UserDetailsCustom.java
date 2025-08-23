package phankhanh.book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import phankhanh.book_store.repository.UserRepository;
import phankhanh.book_store.util.CustomUserDetails;

import java.util.List;

//@Component("userDetailsService")
//public class UserDetailsCustom implements UserDetailsService {
//    private final UserService userService;
//    public UserDetailsCustom(UserService userService) {
//        this.userService = userService;
//    }
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//     phankhanh.book_store.domain.User user = this.userService.getUserByUsername(username);
//        if(user == null) {
//            throw new UsernameNotFoundException("User not found with username: " + username);
//        }
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(),
//                user.getPassword(),
//                Collections.singletonList(new SimpleGrantedAuthority("USER"))
//        );
//    }
//}

@Component("userDetailsService")
@RequiredArgsConstructor
public class UserDetailsCustom implements UserDetailsService {

    private final UserRepository userRepo; // hoặc UserService

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String roleName = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";
        if (!roleName.startsWith("ROLE_")) roleName = "ROLE_" + roleName;

        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())              // dùng đúng field password đã hash
                .authorities(List.of(new SimpleGrantedAuthority(roleName)))
                .enabled(user.isEnabled())
                .build();
    }
}



