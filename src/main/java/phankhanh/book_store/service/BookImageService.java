// src/main/java/phankhanh/book_store/service/BookImageService.java
package phankhanh.book_store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import phankhanh.book_store.DTO.response.BookImageResponse;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.BookImage;
import phankhanh.book_store.repository.BookImageRepository;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.util.BookMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookImageService {

    private final StorageService storageService;
    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;

    /**
     * Thêm nhiều ảnh cho 1 sách:
     * - Nếu sách CHƯA có ảnh chính (sortOrder=0) thì file đầu tiên sẽ làm ảnh chính.
     * - Các ảnh còn lại nối đuôi theo sortOrder tăng dần.
     */
    @Transactional
    public List<BookImageResponse> addImages(Long bookId, List<MultipartFile> files) throws Exception {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files uploaded");
        }
        // (tuỳ m) giới hạn số ảnh mỗi request
        if (files.size() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many files (max 10)");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        // lấy sort lớn nhất hiện có để nối đuôi
        Integer maxSort = bookImageRepository.findMaxSortOrderByBookId(bookId);
        int nextSort = (maxSort == null ? 0 : maxSort) + 1;

        boolean hasMain = bookImageRepository.findFirstByBookIdAndSortOrder(bookId, 0).isPresent();

        List<String> uploadedUrls = new ArrayList<>();
        List<BookImage> toSave = new ArrayList<>();

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile f = files.get(i);

                // lưu file lên Firebase Storage dưới prefix theo bookId cho gọn
                String prefix = "books/%d".formatted(bookId);
                String url = storageService.uploadPublic(f, prefix);
                uploadedUrls.add(url);

                int sort;
                if (!hasMain && i == 0) {
                    sort = 0;           // file đầu tiên làm ảnh chính nếu chưa có
                    hasMain = true;
                } else {
                    sort = nextSort++;  // còn lại nối đuôi
                }

                toSave.add(BookImage.builder()
                        .book(book)
                        .url(url)
                        .sortOrder(sort)
                        .build());
            }

            List<BookImage> saved = bookImageRepository.saveAll(toSave);

            // map sang DTO (có variants) bằng BookMapper m đã dùng
            return saved.stream().map(BookMapper::toImageResponse).toList();

        } catch (RuntimeException ex) {
            // rollback file đã up nếu DB fail
            uploadedUrls.forEach(storageService::delete);
            throw ex;
        } catch (Exception ex) {
            uploadedUrls.forEach(storageService::delete);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", ex);
        }
    }
}
