package lab1.repository;

import lab1.domain.OrderItem;
import lab1.repository.projection.ProductSalesView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** JPQL-звіт: які товари купують найчастіше (GROUP BY + ORDER BY, projection). */
    @Query("""
            select oi.product.id as productId,
                   oi.product.name as productName,
                   sum(oi.quantity) as totalQuantity
            from OrderItem oi
            group by oi.product.id, oi.product.name
            order by sum(oi.quantity) desc, oi.product.name asc
            """)
    List<ProductSalesView> findTopSellingProducts(Pageable pageable);
}
