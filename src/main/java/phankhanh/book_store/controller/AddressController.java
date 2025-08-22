package phankhanh.book_store.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.DTO.request.ReqAddressCreate;
import phankhanh.book_store.DTO.response.ResAddressDTO;
import phankhanh.book_store.DTO.response.RestResponse;
import phankhanh.book_store.service.AddressService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/address")
public class AddressController {
    private final AddressService addressService;
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    private Long currentUserId(Jwt jwt) {
        return jwt.getClaim("userId");
    }
    @GetMapping
    public ResponseEntity<List<ResAddressDTO>> listAddress (@AuthenticationPrincipal Jwt jwt) {
        var data = this.addressService.listMyAddress(jwt.getClaim("userId"));
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<ResAddressDTO> createAddress(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ReqAddressCreate req) {
        Long userId = currentUserId(jwt);
        var data = this.addressService.createMyAddress(userId, req);
        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResAddressDTO> updateAddress(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReqAddressCreate req, @PathVariable Long id ){
            var data = this.addressService.updateMyAddress(currentUserId(jwt), id, req);
            return ResponseEntity.ok(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        this.addressService.deleteMyAddress(currentUserId(jwt), id);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResAddressDTO> setDefault(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable Long id) {
        var data = addressService.setDefault(currentUserId(jwt), id);
        return ResponseEntity.ok(data);
    }
}
