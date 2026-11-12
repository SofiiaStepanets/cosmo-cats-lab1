package lab1.service;

import java.math.BigDecimal;

import lab1.exception.ResourceNotFoundException;
import lab1.domain.Category;
import lab1.domain.Product;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Модульні (unit) тести ProductService: репозиторії замінені моками, база не потрібна.
 * Роботу з реальною базою перевіряє lab1.integration.ProductServiceIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category food;
    private Product product;

    @BeforeEach
    void setUp() {
        food = new Category();
        food.setId(1L);
        food.setName("Food");

        Category categoryFromDto = new Category();
        categoryFromDto.setName("Food");

        product = new Product();
        product.setName("Cosmic Cat Food");
        product.setDescription("Food for intergalactic cats");
        product.setPrice(new BigDecimal("19.99"));
        product.setInStock(true);
        product.setCategory(categoryFromDto);
    }

    @Test
    void createProductShouldReplaceCategoryWithOneFromDatabase() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(food));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.createProduct(product);

        assertSame(food, result.getCategory());
        verify(productRepository).save(product);
    }

    @Test
    void createProductShouldThrowWhenCategoryNotFound() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(product));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProductsShouldReturnRepositoryResult() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        assertEquals(List.of(product), productService.getAllProducts());
    }

    @Test
    void getProductByIdShouldReturnProductWhenFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertSame(product, productService.getProductById(1L));
    }

    @Test
    void getProductByIdShouldThrowWhenNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void updateProductShouldSetIdFromPathAndSave() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(food));
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        product.setId(777L);
        Product result = productService.updateProduct(1L, product);

        assertEquals(1L, result.getId());
        assertSame(food, result.getCategory());
    }

    @Test
    void updateProductShouldThrowWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(999L, product));
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProductShouldDeleteWhenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertTrue(productService.deleteProduct(1L));
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProductShouldReturnFalseWhenNotFound() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertFalse(productService.deleteProduct(999L));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void updateProductShouldThrowWhenCategoryNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(1L, product));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProductShouldIgnoreIdFromRequest() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(food));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        product.setId(777L);
        Product result = productService.createProduct(product);

        assertNull(result.getId());
    }
}
