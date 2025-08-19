package phankhanh.book_store.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultPaginationDTO<T> {
    private Meta meta;
    private T result;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class Meta {
        private int page;       // 0-based
        private int pageSize;
        private int pages;
        private long total;
    }

    /** Helper: map trực tiếp từ Page -> ResultPaginationDTO<List<R>> */
    public static <R> ResultPaginationDTO<List<R>> of(Page<R> page) {
        Meta m = new Meta(page.getNumber(), page.getSize(), page.getTotalPages(), page.getTotalElements());
        return new ResultPaginationDTO<>(m, page.getContent());
    }
}
