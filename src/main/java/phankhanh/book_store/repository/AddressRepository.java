package phankhanh.book_store.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.Address;

import java.util.List;
import java.util.Optional;


public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
        select a from Address a
        where a.user.id = :userId and a.deleted = false
        order by a.isDefault desc, a.updatedAt desc
    """)
    List<Address> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("""
        select a from Address a
        where a.id = :id and a.user.id = :userId and a.deleted = false
    """)
    Optional<Address> findByIdAndUserIdActive(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("""
        update Address a set a.isDefault = false
        where a.user.id = :userId and a.deleted = false and a.isDefault = true
    """)
    int clearDefaultForUser(@Param("userId") Long userId);

    @Query("""
        select count(a) > 0 from Address a
        where a.user.id = :userId and a.deleted = false and a.isDefault = true
    """)
    boolean existsDefaultByUser(@Param("userId") Long userId);

    @Query("""
        select a from Address a
        where a.user.id = :userId and a.deleted = false and a.isDefault = true
    """)
    Optional<Address> findDefaultByUser(@Param("userId") Long userId);
}
