package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phankhanh.book_store.DTO.request.ReqRateBook;
import phankhanh.book_store.DTO.response.ResRating;
import phankhanh.book_store.DTO.response.ResRatingSummary;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.Rating;
import phankhanh.book_store.domain.User;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.RatingRepository;
import phankhanh.book_store.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    private static void validateScore(Integer s) {
        if (s == null || s < 1 || s > 5) throw new IllegalArgumentException("Score must be 1..5");
    }
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Transactional
    public ResRating upsert(Long bookId, Long userId, ReqRateBook req) {
        validateScore(req.score());
        Book book = bookRepo.getReferenceById(bookId);
        User user = userRepo.getReferenceById(userId);

        String content = trimToNull(req.content());
        var opt = ratingRepo.findByBook_IdAndUser_IdAndDeletedAtIsNull(bookId, userId);

        if(opt.isEmpty()) {
            //create
            Rating r = Rating.builder()
                    .book(book)
                    .user(user)
                    .score(req.score())
                    .content(content)
                    .createdAt(java.time.Instant.now())
                    .build();
            r = ratingRepo.save(r);
            int count = Optional.ofNullable(book.getRatingCount()).orElse(0);
            BigDecimal avg = Optional.ofNullable(book.getRatingAvg()).orElse(BigDecimal.ZERO);
            BigDecimal total = avg.multiply(BigDecimal.valueOf(count));
            BigDecimal newAvg = total.add(BigDecimal.valueOf(req.score()))
                    .divide(BigDecimal.valueOf(count + 1), 2, RoundingMode.HALF_UP);
            book.setRatingCount(count + 1);
            book.setRatingAvg(newAvg);

            String name = Optional.ofNullable(user.getFullName()).orElse(user.getEmail());
            return new ResRating(r.getId(), userId, name, r.getScore(), r.getContent(),
                    r.getCreatedAt(), r.getUpdatedAt());
        }else {
            // update
            Rating r = opt.get();
            int old = r.getScore();
            r.setScore(req.score());
            r.setContent(content);
            r.setUpdatedAt(Instant.now());


            int count = book.getRatingCount();
            BigDecimal total = book.getRatingAvg().multiply(BigDecimal.valueOf(count));
            BigDecimal newAvg = total
                    .subtract(BigDecimal.valueOf(old))
                    .add(BigDecimal.valueOf(req.score()))
                    .divide(BigDecimal.valueOf(Math.max(count, 1)), 2, RoundingMode.HALF_UP);
            book.setRatingAvg(newAvg);

            String name = Optional.ofNullable(user.getFullName()).orElse(user.getEmail());
            return new ResRating(r.getId(), userId, name, r.getScore(), r.getContent(),
                    r.getCreatedAt(), r.getUpdatedAt());
        }
    }
    @Transactional
    public void delete(Long ratingId, Long userId, boolean isAdmin) {
        Rating r = ratingRepo.findByIdAndDeletedAtIsNull(ratingId)
                .orElseThrow(() -> new NoSuchElementException("Rating not found"));
        if (!isAdmin && !Objects.equals(r.getUser().getId(), userId)) {
            throw new SecurityException("Not allowed");
        }
        if (r.getDeletedAt() != null) return;

        Book book = r.getBook();
        int count = Optional.ofNullable(book.getRatingCount()).orElse(0);

        if (count > 1) {
            BigDecimal total = book.getRatingAvg().multiply(BigDecimal.valueOf(count));
            BigDecimal newAvg = total.subtract(BigDecimal.valueOf(r.getScore()))
                    .divide(BigDecimal.valueOf(count - 1), 2, RoundingMode.HALF_UP);
            book.setRatingAvg(newAvg);
            book.setRatingCount(count - 1);
        } else {
            book.setRatingAvg(BigDecimal.ZERO);
            book.setRatingCount(0);
        }
        r.setDeletedAt(Instant.now());
    }
    @Transactional
    public ResRatingSummary summary(Long bookId) {
        Book b = bookRepo.getReferenceById(bookId);
        int count = Optional.ofNullable(b.getRatingCount()).orElse(0);
        BigDecimal avg = Optional.ofNullable(b.getRatingAvg()).orElse(BigDecimal.ZERO);

        Map<Integer,Integer> dist = new LinkedHashMap<>();
        Map<Integer,BigDecimal> pct = new LinkedHashMap<>();
        for (int s = 5; s >= 1; s--) {
            int c = Math.toIntExact(ratingRepo.countByBook_IdAndScoreAndDeletedAtIsNull(bookId, s));
            dist.put(s, c);
            BigDecimal p = count == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(c).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            pct.put(s, p);
        }
        return new ResRatingSummary(avg, count, dist, pct);
    }
    @Transactional
    public Optional<ResRating> myRating(Long bookId, Long userId) {
        return ratingRepo.findByBook_IdAndUser_IdAndDeletedAtIsNull(bookId, userId)
                .map(r -> new ResRating(
                        r.getId(),
                        r.getUser().getId(),
                        Optional.ofNullable(r.getUser().getFullName()).orElse(r.getUser().getEmail()),
                        r.getScore(), r.getContent(),
                        r.getCreatedAt(), r.getUpdatedAt()
                ));
    }
    @Transactional
    public Page<ResRating> listReviews(Long bookId, Pageable pageable) {
        return ratingRepo
                .findByBook_IdAndDeletedAtIsNullAndContentIsNotNullAndContentNot(bookId, "", pageable)
                .map(r -> new ResRating(
                        r.getId(),
                        r.getUser().getId(),
                        Optional.ofNullable(r.getUser().getFullName()).orElse(r.getUser().getEmail()),
                        r.getScore(), r.getContent(),
                        r.getCreatedAt(), r.getUpdatedAt()
                ));
    }
}
