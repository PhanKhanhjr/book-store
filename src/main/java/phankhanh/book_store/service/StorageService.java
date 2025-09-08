package phankhanh.book_store.service;

import com.google.firebase.cloud.StorageClient;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {
    private final StorageClient storageClient;
    private final String bucket;

    public StorageService(StorageClient storageClient,
                          @Value("${app.firebase.bucket}") String bucket) {
        this.storageClient = storageClient;
        this.bucket = bucket;
    }

    private String buildObjectKey(String prefix, String filename) {
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) ext = filename.substring(dot);
        return (prefix == null ? "images/books" : prefix) + "/"
                + LocalDate.now() + "/" + UUID.randomUUID() + ext;
    }

    /** Upload và set public-read, trả về public URL */
    public String uploadPublic(MultipartFile file, String prefix) throws IOException {
        String key = buildObjectKey(prefix, file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        Storage gcs = storageClient.bucket(bucket).getStorage();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, key)
                .setContentType(contentType)
                .setCacheControl("public, max-age=31536000, immutable")
                .build();

        gcs.create(blobInfo, file.getBytes());

        // set ACL allUsers:READER (public)
        gcs.update(gcs.get(blobInfo.getBlobId()).toBuilder()
                .setAcl(java.util.List.of(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                .build());

        return "https://storage.googleapis.com/" + bucket + "/" +
                URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    /** Upload private và trả về signed GET URL (hết hạn) */
    public String uploadPrivateAndSign(MultipartFile file, String prefix, long expireSeconds) throws IOException {
        String key = buildObjectKey(prefix, file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        Storage gcs = storageClient.bucket(bucket).getStorage();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, key)
                .setContentType(contentType)
                .setCacheControl("private, max-age=0, no-cache")
                .build();

        gcs.create(blobInfo, file.getBytes());

        return gcs.signUrl(blobInfo, expireSeconds, TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()).toString();
    }

    public boolean delete(String objectKey) {
        Storage gcs = storageClient.bucket(bucket).getStorage();
        return gcs.delete(bucket, objectKey);
    }
}
