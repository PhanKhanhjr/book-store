package phankhanh.book_store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import phankhanh.book_store.DTO.response.ResBookFavoriteDTO;
import phankhanh.book_store.domain.Favorite;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUser_IdAndBook_Id(Long userId, Long bookId);

    @Query("""
    select new phankhanh.book_store.DTO.response.ResBookFavoriteDTO(
        b.id,
        b.title,
        b.slug,
        (select min(i.url) from BookImage i where i.book.id = b.id),
        b.price,
        b.salePrice,
        true,
        (select count(f2) from Favorite f2 where f2.book.id = b.id)
    )
    from Favorite f
    join f.book b
    where f.user.id = :userId
""")
    Page<ResBookFavoriteDTO> findFavoriteBooksByUser(Long userId, Pageable pageable);
    long countByBook_Id(Long bookId);
    boolean existsByUser_IdAndBook_Id(Long userId, Long bookId);
}

