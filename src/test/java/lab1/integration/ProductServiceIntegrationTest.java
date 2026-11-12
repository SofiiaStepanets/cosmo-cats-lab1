package lab1.integration;

import java.math.BigDecimal;

import lab1.exception.ResourceNotFoundException;
import lab1.domain.Category;
import lab1.domain.Product;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
import lab1.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(ProductService.class)
class ProductServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager em;

    private Category food;
    private Category toys;

    @BeforeEach
    void setUp() {
        food = categoryRepository.save(category("Food"));
        toys = categoryRepository.save(category("Toys"));
    }

    // ---------- create ----------

    @Test
    void createProductShouldPersistProductWithExistingCategory() {
        Product created = productService.createProduct(product("Star Cat", 19.99, "Food"));

        assertNotNull(created.getId());

        flushAndClear();

        Product fromDb = productRepository.findById(created.getId()).orElseThrow();
        assertEquals("Star Cat", fromDb.getName());
        assertEquals(0, new BigDecimal("19.99").compareTo(fromDb.getPrice()));
        assertTrue(fromDb.isInStock());
        assertEquals(food.getId(), fromDb.getCategory().getId());
        assertEquals("Food", fromDb.getCategory().getName());
    }

    @Test
    void createProductShouldThrowWhenCategoryDoesNotExist() {
        Product product = product("Star Cat", 19.99, "Unknown");

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(product));
        assertEquals(0, productRepository.count());
    }

    // ---------- read ----------

    @Test
    void getAllProductsShouldReturnAllSavedProducts() {
        productService.createProduct(product("Star Cat", 19.99, "Food"));
        productService.createProduct(product("Galaxy Ball", 9.99, "Toys"));

        List<Product> all = productService.getAllProducts();

        assertEquals(2, all.size());
    }

    @Test
    void getProductByIdShouldReturnProduct() {
        Product created = productService.createProduct(product("Star Cat", 19.99, "Food"));
        flushAndClear();

        Product found = productService.getProductById(created.getId());

        assertEquals("Star Cat", found.getName());
    }

    @Test
    void getProductByIdShouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999_999L));
    }

    // ---------- update ----------

    @Test
    void updateProductShouldChangeFieldsAndKeepId() {
        Product created = productService.createProduct(product("Star Cat", 19.99, "Food"));
        flushAndClear();

        Product changes = product("Comet Cat", 25.00, "Toys");
        changes.setId(12345L); // id з тіла запиту має ігноруватися
        productService.updateProduct(created.getId(), changes);
        flushAndClear();

        assertEquals(1, productRepository.count());
        Product fromDb = productRepository.findById(created.getId()).orElseThrow();
        assertEquals("Comet Cat", fromDb.getName());
        assertEquals(0, new BigDecimal("25.00").compareTo(fromDb.getPrice()));
        assertEquals(toys.getId(), fromDb.getCategory().getId());
    }

    @Test
    void updateProductShouldThrowWhenProductNotFound() {
        Product changes = product("Comet Cat", 25.00, "Food");

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(999_999L, changes));
    }

    @Test
    void updateProductShouldThrowWhenCategoryNotFound() {
        Product created = productService.createProduct(product("Star Cat", 19.99, "Food"));
        Product changes = product("Comet Cat", 25.00, "Unknown");

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(created.getId(), changes));
    }

    // ---------- delete ----------

    @Test
    void deleteProductShouldRemoveExistingProduct() {
        Product created = productService.createProduct(product("Star Cat", 19.99, "Food"));

        boolean deleted = productService.deleteProduct(created.getId());
        flushAndClear();

        assertTrue(deleted);
        assertFalse(productRepository.existsById(created.getId()));
    }

    @Test
    void deleteProductShouldReturnFalseWhenNotFound() {
        assertFalse(productService.deleteProduct(999_999L));
    }

    // ---------- обмеження з міграцій Liquibase ----------

    @Test
    void sameNameInSameCategoryShouldViolateUniqueConstraint() {
        productService.createProduct(product("Star Cat", 19.99, "Food"));
        flushAndClear();

        Product duplicate = product("Star Cat", 30.00, "Food");
        duplicate.setCategory(categoryRepository.findByName("Food").orElseThrow());

        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(duplicate));
    }

    @Test
    void sameNameInDifferentCategoriesShouldBeAllowed() {
        productService.createProduct(product("Star Cat", 19.99, "Food"));
        productService.createProduct(product("Star Cat", 19.99, "Toys"));
        flushAndClear();

        assertEquals(2, productRepository.count());
    }

    @Test
    void negativePriceShouldViolateCheckConstraint() {
        Product product = product("Star Cat", -1.00, "Food");
        product.setCategory(food);

        assertThrows(DataAccessException.class, () -> productRepository.saveAndFlush(product));
    }

    // ---------- допоміжні методи ----------

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private static Category category(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(name + " for cosmic cats");
        category.setActive(true);
        return category;
    }

    /** Товар як він приходить із маппера: категорія лише з назвою, без id. */
    private static Product product(String name, double price, String categoryName) {
        Category category = new Category();
        category.setName(categoryName);

        Product product = new Product();
        product.setName(name);
        product.setDescription("Product for cosmic cats");
        product.setPrice(BigDecimal.valueOf(price));
        product.setInStock(true);
        product.setCategory(category);
        return product;
    }
}
