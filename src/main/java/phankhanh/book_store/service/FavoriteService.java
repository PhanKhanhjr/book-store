package phankhanh.book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.response.ResBookFavoriteDTO;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.Favorite;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.FavoriteRepository;
import phankhanh.book_store.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepo;
    private final UserRepository userRepo;
    private final BookRepository bookRepo;

    public boolean toggleFavorite(Long userId, Long bookId) {
        var existing = favoriteRepo.findByUser_IdAndBook_Id(userId, bookId);
        if(existing.isPresent()) {
            favoriteRepo.delete(existing.get());
            return false;
        }

        var user = userRepo.findById(userId).orElseThrow();
        var book = bookRepo.findById(bookId).orElseThrow();
        favoriteRepo.save(Favorite.builder().user(user).book(book).build());
        return true;
    }

    public Page<ResBookFavoriteDTO> getUserFavorites(Long userId, Pageable pageable) {
        return favoriteRepo.findFavoriteBooksByUser(userId, pageable);
    }

    public long countFavorites(Long bookId) {
        return favoriteRepo.countByBook_Id(bookId);
    }

    public boolean likedByMe(Long userId, Long bookId) {
        return favoriteRepo.findByUser_IdAndBook_Id(userId, bookId).isPresent();
    }
}
