package phankhanh.book_store.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${GOOGLE_APPLICATION_CREDENTIALS:/etc/missing.json}") // đọc từ ENV hoặc property cùng tên
    private String credsPath;

    @Value("${app.firebase.bucket}") // tuỳ dùng hay không
    private String bucket;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        try (var in = new java.io.FileInputStream(credsPath)) {
            var builder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in));
            if (bucket != null && !bucket.isBlank()) {
                builder.setStorageBucket(bucket);
            }
            var opts = builder.build();
            return FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(opts) : FirebaseApp.getInstance();
        }
    }
    @Bean
    public StorageClient storageClient(FirebaseApp app) {
        return StorageClient.getInstance(app);
    }
}


