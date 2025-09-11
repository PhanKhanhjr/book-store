package phankhanh.book_store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import phankhanh.book_store.DTO.request.ReqCategoryCreate;
import phankhanh.book_store.DTO.response.CategoryDTO;
import phankhanh.book_store.DTO.response.ResBookListItemDTO;
import phankhanh.book_store.domain.Book;
import phankhanh.book_store.domain.Category;
import phankhanh.book_store.repository.BookRepository;
import phankhanh.book_store.repository.CategoryRepository;
import phankhanh.book_store.util.BookMapper;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }
    private CategoryDTO toDto(Category c) {
        return new CategoryDTO(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getChildren().stream().map(this::toDto).toList()
        );
    }


    @Transactional
    public CategoryDTO create(ReqCategoryCreate req) {
        Category cat = new Category();
        cat.setName(req.getName());

        if (req.getParentId() != null) {
            cat.setParent(categoryRepository.getReferenceById(req.getParentId()));
        } else if (req.getParentSlug() != null && !req.getParentSlug().isBlank()) {
            Category parent = categoryRepository.findBySlug(req.getParentSlug())
                    .orElseThrow(() -> new IllegalArgumentException("Parent not found"));
            cat.setParent(parent);
        }
        return this.toDto(categoryRepository.save(cat));
    }
}
