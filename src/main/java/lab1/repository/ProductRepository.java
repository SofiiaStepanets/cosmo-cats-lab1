package lab1.repository;

import lab1.domain.Product;
import lab1.repository.projection.ProductSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCategoryId(Long categoryId);

    /** JPQL: WHERE + ORDER BY, результат обмежений projection. */
    @Query("""
            select p.id as id, p.name as name, p.price as price
            from Product p
            where p.category.name = :categoryName and p.inStock = true
            order by p.price asc
            """)
    List<ProductSummaryView> findInStockByCategory(@Param("categoryName") String categoryName);

    /** Іменований метод: WHERE price BETWEEN ... ORDER BY price. */
    List<ProductSummaryView> findByPriceBetweenOrderByPriceAsc(BigDecimal min, BigDecimal max);
}
