package phankhanh.book_store.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import phankhanh.book_store.DTO.response.BookImageResponse;
import phankhanh.book_store.service.BookImageService;
import phankhanh.book_store.service.BookService;
import phankhanh.book_store.service.StorageService;

import java.util.List;

@RestController
@RequestMapping("/api/uploads")
public class BookImageController {
    private final StorageService storageService;
    private final BookService bookService;
    private final BookImageService bookImageService;

    public BookImageController(StorageService storageService, BookService bookService, BookImageService bookImageService) {
        this.bookService = bookService;
        this.storageService = storageService;
        this.bookImageService = bookImageService;
    }

    // Đơn giản: upload → public URL
    @PostMapping(path = "/public", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPublic(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "prefix", required = false) String prefix) throws Exception {
        String url = storageService.uploadPublic(file, prefix);
        return ResponseEntity.ok(new UploadResponse(url, null));
    }

    // An toàn hơn: upload private → trả về signed GET URL
    @PostMapping(path = "/private", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPrivate(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "prefix", required = false) String prefix,
                                           @RequestParam(value = "ttl", required = false, defaultValue = "900") long ttlSeconds) throws Exception {
        String signedUrl = storageService.uploadPrivateAndSign(file, prefix, ttlSeconds);
        return ResponseEntity.ok(new UploadResponse(null, signedUrl));
    }

    // Xóa theo objectKey (lưu objectKey trong DB khi tạo)
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("objectKey") String objectKey) {
        boolean ok = storageService.delete(objectKey);
        return ResponseEntity.ok(java.util.Map.of("deleted", ok));
    }

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookImageResponse>> addImages(
            @PathVariable("id") Long bookId,
            @RequestParam("file") List<MultipartFile> files
    ) throws Exception {
        return ResponseEntity.ok(bookImageService.addImages(bookId, files));
    }

    record UploadResponse(String publicUrl, String signedUrl) {}
}
