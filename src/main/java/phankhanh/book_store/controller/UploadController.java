//package phankhanh.book_store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import phankhanh.book_store.service.StorageService;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
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

    record UploadResponse(String publicUrl, String signedUrl) {}
}
