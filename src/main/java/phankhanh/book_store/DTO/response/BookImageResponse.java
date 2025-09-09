package phankhanh.book_store.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookImageResponse {
    private Long id;
    private String url;
    private Integer sortOrder; // 0 = ảnh chính
    private Variants variants; // t/_m/_l/_xl

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Variants {
        private String thumb;   // 160x240
        private String medium;  // 320x480
        private String large;   // 640x960
        private String xlarge;  // 960x1440
    }
}