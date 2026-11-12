package lab1.integration;

import java.math.BigDecimal;

import lab1.exception.ResourceNotFoundException;
import lab1.domain.Category;
import lab1.domain.Product;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
import lab1.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(CategoryService.class)
class CategoryServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager em;

    // ---------- create ----------

    @Test
    void createCategoryShouldPersistAllFields() {
        Category created = categoryService.createCategory(category("Food"));

        assertNotNull(created.getId());

        flushAndClear();

        Category fromDb = categoryRepository.findById(created.getId()).orElseThrow();
        assertEquals("Food", fromDb.getName());
        assertEquals("Food for cosmic cats", fromDb.getDescription());
        assertTrue(fromDb.isActive());
    }

    // ---------- read ----------

    @Test
    void getAllCategoriesShouldReturnAllSaved() {
        categoryService.createCategory(category("Food"));
        categoryService.createCategory(category("Toys"));

        List<Category> all = categoryService.getAllCategories();

        assertEquals(2, all.size());
    }

    @Test
    void getCategoryByIdShouldReturnCategory() {
        Category created = categoryService.createCategory(category("Food"));
        flushAndClear();

        Category found = categoryService.getCategoryById(created.getId());

        assertEquals("Food", found.getName());
    }

    @Test
    void getCategoryByIdShouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(999_999L));
    }

    @Test
    void findByNameShouldFindCategoryByName() {
        categoryService.createCategory(category("Food"));
        flushAndClear();

        assertTrue(categoryRepository.findByName("Food").isPresent());
        assertTrue(categoryRepository.findByName("Unknown").isEmpty());
    }

    // ---------- update ----------

    @Test
    void updateCategoryShouldRenameAndKeepId() {
        Category created = categoryService.createCategory(category("Food"));
        flushAndClear();

        Category changes = category("Cat Food");
        changes.setActive(false);
        categoryService.updateCategory(created.getId(), changes);
        flushAndClear();

        assertEquals(1, categoryRepository.count());
        Category fromDb = categoryRepository.findById(created.getId()).orElseThrow();
        assertEquals("Cat Food", fromDb.getName());
        assertFalse(fromDb.isActive());
    }

    @Test
    void updateCategoryShouldThrowWhenNotFound() {
        Category changes = category("Cat Food");

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999_999L, changes));
    }

    // ---------- delete ----------

    @Test
    void deleteCategoryShouldRemoveEmptyCategory() {
        Category created = categoryService.createCategory(category("Food"));

        boolean deleted = categoryService.deleteCategory(created.getId());
        flushAndClear();

        assertTrue(deleted);
        assertFalse(categoryRepository.existsById(created.getId()));
    }

    @Test
    void deleteCategoryShouldNotRemoveCategoryWithProducts() {
        Category food = categoryService.createCategory(category("Food"));
        Product product = productRepository.save(product("Star Cat", food));
        flushAndClear();

        boolean deleted = categoryService.deleteCategory(food.getId());
        flushAndClear();

        assertFalse(deleted);
        assertTrue(categoryRepository.existsById(food.getId()));
        assertTrue(productRepository.existsById(product.getId()));
    }

    @Test
    void deleteCategoryShouldReturnFalseWhenNotFound() {
        assertFalse(categoryService.deleteCategory(999_999L));
    }

    @Test
    void existsByCategoryIdShouldDetectProductsInCategory() {
        Category food = categoryService.createCategory(category("Food"));
        Category toys = categoryService.createCategory(category("Toys"));
        productRepository.save(product("Star Cat", food));
        flushAndClear();

        assertTrue(productRepository.existsByCategoryId(food.getId()));
        assertFalse(productRepository.existsByCategoryId(toys.getId()));
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

    private static Product product(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Product for cosmic cats");
        product.setPrice(new BigDecimal("19.99"));
        product.setInStock(true);
        product.setCategory(category);
        return product;
    }
}
