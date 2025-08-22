package phankhanh.book_store.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import phankhanh.book_store.DTO.request.ReqAddressCreate;
import phankhanh.book_store.DTO.response.ResAddressDTO;
import phankhanh.book_store.Repository.AddressRepository;
import phankhanh.book_store.Repository.UserRepository;
import phankhanh.book_store.domain.Address;
import phankhanh.book_store.domain.User;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public List<ResAddressDTO> listMyAddress(Long userId) {
        return this.addressRepository.findAllActiveByUserId(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ResAddressDTO createMyAddress(Long userId, ReqAddressCreate req) throws IllegalArgumentException {
        User user = this.userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Address address = Address.builder()
                .user(user)
                .fullName(req.fullName())
                .phone(req.phone())
                .line1(req.line1())
                .ward(req.ward())
                .district(req.district())
                .province(req.province())
                .build();
        boolean needDefault = req.makeDefault() || !this.addressRepository.existsDefaultByUser(userId);
        if(needDefault) {
            this.addressRepository.clearDefaultForUser(userId);
            address.setDefault(true);
        }
        return toDTO(this.addressRepository.save(address));
    }

    @Transactional
    public ResAddressDTO updateMyAddress (Long userId, Long addressId, ReqAddressCreate req) throws IllegalArgumentException {
        Address address = this.addressRepository.findById(addressId).orElseThrow(() -> new IllegalArgumentException("Address not found"));
        address.setFullName(req.fullName());
        address.setPhone(req.phone());
        address.setLine1(req.line1());
        address.setWard(req.ward());
        address.setDistrict(req.district());
        address.setProvince(req.province());
        if(req.makeDefault() != null && req.makeDefault()) {
            this.addressRepository.clearDefaultForUser(userId);
            address.setDefault(true);
        }
        return toDTO(address);
    }

    @Transactional
    public void deleteMyAddress(Long userId, Long addressId){
        Address address = this.addressRepository.findById(addressId).orElseThrow(() -> new IllegalArgumentException("Address not found"));
        boolean wasDefault = address.isDefault();
        address.setDeleted(true);
        address.setDefault(false);
        if(wasDefault) {
            var others = this.addressRepository.findAllActiveByUserId(userId);
            if(!others.isEmpty()){
                others.get(0).setDefault(true);
            }
        }
    }

    @Transactional
    public ResAddressDTO setDefault(Long userId, Long addressId) {
        Address a = this.addressRepository.findByIdAndUserIdActive(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        this.addressRepository.clearDefaultForUser(userId);
        a.setDefault(true);
        return toDTO(a);
    }
    private ResAddressDTO toDTO(Address a) {
        return new ResAddressDTO(
                a.getId(),
                a.getUser().getId(),
                a.getFullName(),
                a.getPhone(),
                a.getLine1(),
                a.getWard(),
                a.getDistrict(),
                a.getProvince(),
                a.isDefault(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
