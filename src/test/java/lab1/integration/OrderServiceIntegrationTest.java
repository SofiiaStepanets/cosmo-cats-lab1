package lab1.integration;

import java.math.BigDecimal;

import lab1.exception.ResourceNotFoundException;
import lab1.domain.Category;
import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.repository.CategoryRepository;
import lab1.repository.OrderItemRepository;
import lab1.repository.OrderRepository;
import lab1.repository.ProductRepository;
import lab1.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(OrderService.class)
class OrderServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager em;

    private Product starCat;
    private Product galaxyBall;

    @BeforeEach
    void setUp() {
        Category food = new Category();
        food.setName("Food");
        food.setActive(true);
        food = categoryRepository.save(food);

        starCat = productRepository.save(product("Star Cat", food));
        galaxyBall = productRepository.save(product("Galaxy Ball", food));
    }

    // ---------- create ----------

    @Test
    void createOrderShouldPersistOrderWithItems() {
        Order created = orderService.createOrder(order(item(starCat.getId(), 2), item(galaxyBall.getId(), 1)));

        assertNotNull(created.getId());

        flushAndClear();

        Order fromDb = orderRepository.findById(created.getId()).orElseThrow();
        assertEquals(2, fromDb.getItems().size());
        for (OrderItem item : fromDb.getItems()) {
            assertNotNull(item.getId());
            assertEquals(created.getId(), item.getOrder().getId());
        }
        List<String> productNames = fromDb.getItems().stream()
                .map(i -> i.getProduct().getName())
                .sorted()
                .toList();
        assertEquals(List.of("Galaxy Ball", "Star Cat"), productNames);
    }

    @Test
    void createOrderShouldGenerateNaturalId() {
        Order created = orderService.createOrder(order(item(starCat.getId(), 1)));

        assertNotNull(created.getNaturalOrderId());
        // номер має формат UUID — інакше fromString кине виняток
        assertDoesNotThrow(() -> UUID.fromString(created.getNaturalOrderId()));
    }

    @Test
    void naturalIdsShouldBeUniqueForDifferentOrders() {
        Order first = orderService.createOrder(order(item(starCat.getId(), 1)));
        Order second = orderService.createOrder(order(item(starCat.getId(), 1)));

        assertNotEquals(first.getNaturalOrderId(), second.getNaturalOrderId());
    }

    @Test
    void createOrderWithoutItemsShouldStillGetNaturalId() {
        Order created = orderService.createOrder(order());
        flushAndClear();

        Order fromDb = orderRepository.findById(created.getId()).orElseThrow();
        assertNotNull(fromDb.getNaturalOrderId());
        assertTrue(fromDb.getItems().isEmpty());
    }

    @Test
    void createOrderShouldThrowWhenProductDoesNotExist() {
        Order order = order(item(999_999L, 1));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(order));
    }

    @Test
    void orderDateShouldBeFilledByDatabaseDefault() {
        Order created = orderService.createOrder(order(item(starCat.getId(), 1)));
        flushAndClear();

        Object orderDate = em.getEntityManager()
                .createNativeQuery("SELECT OrderDate FROM Orders WHERE OrderId = :id")
                .setParameter("id", created.getId())
                .getSingleResult();

        assertNotNull(orderDate);
    }

    // ---------- read ----------

    @Test
    void getAllOrdersShouldReturnAllSaved() {
        orderService.createOrder(order(item(starCat.getId(), 1)));
        orderService.createOrder(order(item(galaxyBall.getId(), 3)));

        assertEquals(2, orderService.getAllOrders().size());
    }

    @Test
    void getOrderByIdShouldReturnOrder() {
        Order created = orderService.createOrder(order(item(starCat.getId(), 1)));
        flushAndClear();

        Order found = orderService.getOrderById(created.getId());

        assertEquals(created.getNaturalOrderId(), found.getNaturalOrderId());
    }

    @Test
    void getOrderByIdShouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999_999L));
    }

    @Test
    void getOrderByNaturalIdShouldReturnOrder() {
        Order created = orderService.createOrder(order(item(starCat.getId(), 1)));
        flushAndClear();

        Order found = orderService.getOrderByNaturalId(created.getNaturalOrderId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getOrderByNaturalIdShouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderByNaturalId("no-such-order"));
    }

    // ---------- delete ----------

    @Test
    void deleteOrderShouldRemoveOrderAndItsItems() {
        Order created = orderService.createOrder(order(item(starCat.getId(), 2), item(galaxyBall.getId(), 1)));
        flushAndClear();
        assertEquals(2, orderItemRepository.count());

        boolean deleted = orderService.deleteOrder(created.getId());
        flushAndClear();

        assertTrue(deleted);
        assertFalse(orderRepository.existsById(created.getId()));
        assertEquals(0, orderItemRepository.count());
        // товари при цьому залишаються
        assertEquals(2, productRepository.count());
    }

    @Test
    void deleteOrderShouldReturnFalseWhenNotFound() {
        assertFalse(orderService.deleteOrder(999_999L));
    }

    // ---------- допоміжні методи ----------

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private static Product product(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Product for cosmic cats");
        product.setPrice(new BigDecimal("19.99"));
        product.setInStock(true);
        product.setCategory(category);
        return product;
    }

    /** Позиція як вона приходить із JSON: у товару заповнений лише id. */
    private static OrderItem item(Long productId, int quantity) {
        Product product = new Product();
        product.setId(productId);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private static Order order(OrderItem... items) {
        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(items)));
        return order;
    }
}
