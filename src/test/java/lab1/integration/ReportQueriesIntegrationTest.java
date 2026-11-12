package lab1.integration;

import lab1.domain.Category;
import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
import lab1.repository.projection.ProductSalesView;
import lab1.repository.projection.ProductSummaryView;
import lab1.service.OrderService;
import lab1.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Projections та JPQL-запити (WHERE, GROUP BY, ORDER BY) на справжній MySQL. */
@Import({ReportService.class, OrderService.class})
class ReportQueriesIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager em;

    private Product starTuna;
    private Product galaxyMilk;
    private Product cometBall;

    @BeforeEach
    void setUp() {
        Category food = categoryRepository.save(new Category(null, "Food", "Space food"));
        Category toys = categoryRepository.save(new Category(null, "Toys", "Anti-gravity toys"));

        starTuna = productRepository.save(product("Star Tuna", "19.99", food, true));
        galaxyMilk = productRepository.save(product("Galaxy Milk", "12.50", food, true));
        productRepository.save(product("Star Jerky", "30.00", food, false));
        cometBall = productRepository.save(product("Comet Ball", "24.00", toys, true));
    }

    @Test
    void topSellingProductsShouldBeOrderedByQuantityDesc() {
        orderService.createOrder(order(item(starTuna, 2), item(cometBall, 1)));
        orderService.createOrder(order(item(starTuna, 3), item(galaxyMilk, 4)));
        em.flush();
        em.clear();

        List<ProductSalesView> report = reportService.getTopSellingProducts(10);

        assertEquals(3, report.size());
        assertEquals("Star Tuna", report.get(0).getProductName());
        assertEquals(5L, report.get(0).getTotalQuantity());
        assertEquals("Galaxy Milk", report.get(1).getProductName());
        assertEquals(4L, report.get(1).getTotalQuantity());
        assertEquals("Comet Ball", report.get(2).getProductName());
    }

    @Test
    void topSellingProductsShouldRespectLimit() {
        orderService.createOrder(order(item(starTuna, 2), item(galaxyMilk, 1), item(cometBall, 1)));
        em.flush();

        List<ProductSalesView> report = reportService.getTopSellingProducts(1);

        assertEquals(1, report.size());
        assertEquals(starTuna.getId(), report.get(0).getProductId());
    }

    @Test
    void inStockByCategoryShouldFilterAndSortByPrice() {
        List<ProductSummaryView> food = reportService.getInStockProductsByCategory("Food");

        // "Star Jerky" немає в наявності, "Comet Ball" з іншої категорії
        assertEquals(List.of("Galaxy Milk", "Star Tuna"), food.stream().map(ProductSummaryView::getName).toList());
        assertEquals(0, new BigDecimal("12.50").compareTo(food.get(0).getPrice()));
    }

    @Test
    void findByPriceBetweenShouldReturnSortedRange() {
        List<ProductSummaryView> range = productRepository.findByPriceBetweenOrderByPriceAsc(
                new BigDecimal("15.00"), new BigDecimal("25.00"));

        assertEquals(List.of("Star Tuna", "Comet Ball"), range.stream().map(ProductSummaryView::getName).toList());
    }

    private static Product product(String name, String price, Category category, boolean inStock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Product for cosmic cats");
        product.setPrice(new BigDecimal(price));
        product.setCategory(category);
        product.setInStock(inStock);
        return product;
    }

    private static OrderItem item(Product product, int quantity) {
        Product ref = new Product();
        ref.setId(product.getId());
        return new OrderItem(ref, quantity);
    }

    private static Order order(OrderItem... items) {
        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(items)));
        return order;
    }
}
