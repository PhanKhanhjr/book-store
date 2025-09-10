package phankhanh.book_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import phankhanh.book_store.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);

    List<Category> findByParent_Id(Long parentId);
    List<Category> findByParentIsNull(); // root categories
    @Query(value = """
        WITH RECURSIVE sub AS (
          SELECT id FROM categories WHERE slug = :slug
          UNION ALL
          SELECT c.id
          FROM categories c
          JOIN sub s ON c.parent_id = s.id
        )
        SELECT id FROM sub
        """, nativeQuery = true)
    List<Long> findDescendantIdsBySlug(@Param("slug") String slug);
}
