package phankhanh.book_store.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import phankhanh.book_store.domain.*;
import phankhanh.book_store.util.constant.ProductStatus;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Book> searchBooks(
            String q, Long authorId, Long categoryId, Long publisherId, Long supplierId,
            ProductStatus status, Pageable pageable
    ) {
        boolean hasQ = q != null && !q.trim().isEmpty();
        String qLower = hasQ ? q.trim().toLowerCase(java.util.Locale.ROOT) : "";

        // 1) Lấy ids theo rank (GROUP BY id)
        StringBuilder sb = new StringBuilder("""
    WITH cand AS (
      SELECT b.id,
             MAX(ts_rank(to_tsvector('simple', unaccent(b.title||' '||b.slug||' '||coalesce(b.isbn13,''))),
                          phraseto_tsquery('simple', unaccent(:q))))  AS r_phrase,
             MAX(ts_rank(to_tsvector('simple', unaccent(b.title||' '||b.slug||' '||coalesce(b.isbn13,''))),
                          plainto_tsquery('simple', unaccent(:q))))  AS r_plain,
             MAX(similarity(unaccent(b.title||' '||b.slug||' '||coalesce(b.isbn13,'')), unaccent(:q))) AS r_sim,
             MAX(b.created_at) AS created_max
      FROM books b
      LEFT JOIN book_authors ba ON b.id = ba.book_id
      LEFT JOIN authors a ON a.id = ba.author_id
      LEFT JOIN book_categories bc ON b.id = bc.book_id
      WHERE b.deleted = false
    """);
        if (status != null)      sb.append(" AND b.status = :status");
        if (publisherId != null) sb.append(" AND b.publisher_id = :publisherId");
        if (supplierId != null)  sb.append(" AND b.supplier_id  = :supplierId");
        if (authorId != null)    sb.append(" AND a.id = :authorId");
        if (categoryId != null)  sb.append(" AND bc.category_id = :categoryId");

        if (hasQ) sb.append("""
      AND (
        lower(unaccent(b.title))                LIKE lower(unaccent(:likeAll)) ESCAPE '!' OR
        lower(unaccent(b.slug))                 LIKE lower(unaccent(:likeAll)) ESCAPE '!' OR
        lower(unaccent(coalesce(b.isbn13,'')))  LIKE lower(unaccent(:likeAll)) ESCAPE '!' OR
        lower(unaccent(coalesce(a.name,'')))    LIKE lower(unaccent(:likeAll)) ESCAPE '!'
      )
    """);

        sb.append("""
      GROUP BY b.id
    )
    SELECT id
    FROM cand
    ORDER BY r_phrase DESC, r_plain DESC, r_sim DESC, created_max DESC, id DESC
    OFFSET :off LIMIT :lim
    """);

        jakarta.persistence.Query qIds = em.createNativeQuery(sb.toString());
        qIds.setParameter("off", pageable.getOffset());
        qIds.setParameter("lim", pageable.getPageSize());
        if (status != null)      qIds.setParameter("status", status.name());
        if (publisherId != null) qIds.setParameter("publisherId", publisherId);
        if (supplierId != null)  qIds.setParameter("supplierId", supplierId);
        if (authorId != null)    qIds.setParameter("authorId", authorId);
        if (categoryId != null)  qIds.setParameter("categoryId", categoryId);
        if (hasQ) {
            qIds.setParameter("q", qLower);
            qIds.setParameter("likeAll", "%" + qLower + "%");
        }
        String qParam = hasQ ? qLower : "___noq___";
        qIds.setParameter("q", qParam);

        if (hasQ) {
            qIds.setParameter("likeAll", "%" + qLower + "%");
        }

        @SuppressWarnings("unchecked")
        java.util.List<Number> idNums = qIds.getResultList();
        java.util.List<Long> ids = idNums.stream().map(Number::longValue).toList();
        if (ids.isEmpty()) return new PageImpl<>(java.util.List.of(), pageable, 0);

        // 2) Count tổng
        StringBuilder sbCount = new StringBuilder("""
    SELECT COUNT(DISTINCT b.id)
    FROM books b
    LEFT JOIN book_authors ba ON b.id = ba.book_id
    LEFT JOIN authors a ON a.id = ba.author_id
    LEFT JOIN book_categories bc ON b.id = bc.book_id
    WHERE b.deleted = false
    """);
        if (status != null)      sbCount.append(" AND b.status = :status");
        if (publisherId != null) sbCount.append(" AND b.publisher_id = :publisherId");
        if (supplierId != null)  sbCount.append(" AND b.supplier_id  = :supplierId");
        if (authorId != null)    sbCount.append(" AND a.id = :authorId");
        if (categoryId != null)  sbCount.append(" AND bc.category_id = :categoryId");
        if (hasQ) sbCount.append("""
      AND (
        lower(unaccent(b.title))                LIKE lower(unaccent(:likeAll)) ESCAPE '!' OR
        lower(unaccent(b.slug))                 LIKE lower(unaccent(:likeAll)) ESCAPE '!' OR
        lower(unaccent(coalesce(b.isbn13,'')))  LIKE lower(unaccent(:likeAll)) ESCAPE '!' OR
        lower(unaccent(coalesce(a.name,'')))    LIKE lower(unaccent(:likeAll)) ESCAPE '!'
      )
    """);

        Query qCount = em.createNativeQuery(sbCount.toString());
        if (status != null)      qCount.setParameter("status", status.name());
        if (publisherId != null) qCount.setParameter("publisherId", publisherId);
        if (supplierId != null)  qCount.setParameter("supplierId", supplierId);
        if (authorId != null)    qCount.setParameter("authorId", authorId);
        if (categoryId != null)  qCount.setParameter("categoryId", categoryId);
        if (hasQ) {
            qCount.setParameter("likeAll", "%" + qLower + "%");
        }
        long total = ((Number) qCount.getSingleResult()).longValue();

        // 3) Lấy Book theo ids và giữ thứ tự
        var books = em.createQuery("SELECT b FROM Book b WHERE b.id IN :ids", Book.class)
                .setParameter("ids", ids).getResultList();
        var map = new java.util.HashMap<Long, Book>(books.size());
        for (Book b : books) map.put(b.getId(), b);
        var ordered = new java.util.ArrayList<Book>(ids.size());
        for (Long id : ids) { var b = map.get(id); if (b != null) ordered.add(b); }

        return new PageImpl<>(ordered, pageable, total);
    }









    // BookRepositoryImpl.java (bổ sung các hàm dưới vào class đã có searchBooks)
    @Override
    public List<Book> findNewest(ProductStatus status, int limit) {
        QBook book = QBook.book;
        BooleanBuilder where = new BooleanBuilder().and(book.deleted.isFalse());
        if (status != null) where.and(book.status.eq(status));
        return queryFactory.selectFrom(book)
                .where(where)
                .orderBy(book.createdAt.desc(), book.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Book> findBestSellers(ProductStatus status, int limit) {
        QBook book = QBook.book;
        QInventory inv = QInventory.inventory;

        BooleanBuilder where = new BooleanBuilder().and(book.deleted.isFalse());
        if (status != null) where.and(book.status.eq(status));

        var soldExpr = inv.sold.coalesce(0);

        return queryFactory.selectFrom(book)
                .leftJoin(book.inventory, inv)
                .where(where)
                .orderBy(soldExpr.desc(), book.createdAt.desc(), book.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Book> findOnSale(ProductStatus status, int limit, java.time.Instant now) {
        QBook book = QBook.book;

        BooleanBuilder where = new BooleanBuilder()
                .and(book.deleted.isFalse())
                .and(book.salePrice.isNotNull())
                .and(
                        Expressions.booleanTemplate(
                                "( {0} IS NULL OR {0} <= {2} ) AND ( {1} IS NULL OR {1} >= {2} )",
                                book.saleStartAt, book.saleEndAt, now
                        )
                );
        if (status != null) where.and(book.status.eq(status));

        var discountRate = Expressions.numberTemplate(Double.class,
                "CASE WHEN {0} IS NULL OR {0}=0 THEN 0 " +
                        "ELSE (({0}-{1}) * 1.0 / {0}) END",
                book.price, book.salePrice);


        return queryFactory.selectFrom(book)
                .where(where)
                .orderBy(discountRate.desc(), book.createdAt.desc(), book.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Book> findTopByCategory(Long categoryId, ProductStatus status, int limit) {
        QBook book = QBook.book;
        QCategory category = QCategory.category;

        BooleanBuilder where = new BooleanBuilder()
                .and(book.deleted.isFalse())
                .and(category.id.eq(categoryId));
        if (status != null) where.and(book.status.eq(status));

        return queryFactory.selectFrom(book)
                .leftJoin(book.categories, category)
                .where(where)
                .orderBy(book.createdAt.desc(), book.id.desc())
                .limit(limit)
                .fetch();
    }

}
