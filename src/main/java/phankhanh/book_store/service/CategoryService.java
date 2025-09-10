package phankhanh.book_store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import phankhanh.book_store.DTO.response.ResBookListItemDTO;
import phankhanh.book_store.domain.Book;
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
}
