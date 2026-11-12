package lab1.service;

import lab1.domain.Category;
import lab1.domain.Product;
import lab1.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    private static Product product(String name, String price, String categoryName) {
        Category category = new Category();
        category.setName(categoryName);

        Product product = new Product();
        product.setName(name);
        product.setDescription("Product for cosmic cats");
        product.setPrice(new BigDecimal(price));
        product.setInStock(true);
        product.setCategory(category);
        return product;
    }

    @Test
    void shouldStartWithMockProducts() {
        List<Product> products = productService.getAllProducts();

        assertEquals(3, products.size());
        assertEquals("Star Tuna", products.get(0).getName());
    }

    @Test
    void getAllProductsShouldBeSortedById() {
        List<Product> products = productService.getAllProducts();

        for (int i = 1; i < products.size(); i++) {
            assertTrue(products.get(i - 1).getId() < products.get(i).getId());
        }
    }

    @Test
    void createProductShouldAssignNewIdAndCategoryFromCatalog() {
        Product created = productService.createProduct(product("Comet Bowl", "15.00", "toys"));

        assertEquals(4L, created.getId());
        assertEquals("Toys", created.getCategory().getName());
        assertNotNull(created.getCategory().getId());
        assertEquals(4, productService.getAllProducts().size());
    }

    @Test
    void createProductShouldIgnoreIdFromRequest() {
        Product product = product("Comet Bowl", "15.00", "Toys");
        product.setId(777L);

        Product created = productService.createProduct(product);

        assertEquals(4L, created.getId());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(777L));
    }

    @Test
    void createProductShouldThrowWhenCategoryNotFound() {
        Product product = product("Comet Bowl", "15.00", "Unknown");

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(product));
        assertEquals(3, productService.getAllProducts().size());
    }

    @Test
    void createProductShouldThrowWhenCategoryNameIsNull() {
        Product product = product("Comet Bowl", "15.00", null);

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(product));
    }

    @Test
    void getProductByIdShouldReturnProductWhenFound() {
        Product product = productService.getProductById(1L);

        assertEquals("Star Tuna", product.getName());
        assertEquals(0, new BigDecimal("19.99").compareTo(product.getPrice()));
    }

    @Test
    void getProductByIdShouldThrowWhenNotFound() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(999L));

        assertEquals("Product with id 999 not found.", ex.getMessage());
    }

    @Test
    void updateProductShouldReplaceFieldsAndKeepPathId() {
        Product changes = product("Comet Tuna", "25.00", "Food");
        changes.setId(12345L);

        Product updated = productService.updateProduct(1L, changes);

        assertEquals(1L, updated.getId());
        assertEquals("Comet Tuna", productService.getProductById(1L).getName());
        assertEquals(3, productService.getAllProducts().size());
    }

    @Test
    void updateProductShouldThrowWhenProductNotFound() {
        Product changes = product("Comet Tuna", "25.00", "Food");

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(999L, changes));
    }

    @Test
    void updateProductShouldThrowWhenCategoryNotFound() {
        Product changes = product("Comet Tuna", "25.00", "Unknown");

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, changes));
        assertEquals("Star Tuna", productService.getProductById(1L).getName());
    }

    @Test
    void deleteProductShouldRemoveExistingProduct() {
        assertTrue(productService.deleteProduct(1L));
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void deleteProductShouldReturnFalseWhenNotFound() {
        assertFalse(productService.deleteProduct(999L));
    }
}
