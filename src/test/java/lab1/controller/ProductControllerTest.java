package lab1.controller;

import java.math.BigDecimal;

import lab1.controller.ProductController;
import lab1.domain.Category;
import lab1.domain.Product;
import lab1.exception.GlobalExceptionHandler;
import lab1.exception.ResourceNotFoundException;
import lab1.mapper.ProductMapper;
import lab1.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private ProductService productService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        productService = mock(ProductService.class);

        ProductController controller =
                new ProductController(productService, new ProductMapper());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Product createProduct(
            Long id, String name, double price, String categoryName) {

        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Food for cosmic cats");
        product.setPrice(BigDecimal.valueOf(price));
        product.setInStock(true);

        Category category = new Category();
        category.setName(categoryName);
        product.setCategory(category);

        return product;
    }

    @Test
    void getAllProductsShouldReturnPagedResults() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(
                createProduct(1L, "Star Cat", 19.99, "Food"),
                createProduct(2L, "Galaxy Cat", 29.99, "Toys"),
                createProduct(3L, "Comet Cat", 39.99, "Food")
        ));

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Galaxy Cat"));
    }

    @Test
    void getAllProductsShouldRejectNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProductsShouldRejectZeroSize() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductByIdShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(createProduct(1L, "Star Cat", 19.99, "Food"));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Star Cat"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void createProductShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    saved.setId(42L);
                    return saved;
                });

        String json = """
            {
                "name": "Star Cat",
                "description": "Food for cosmic cats",
                "price": 19.99,
                "category": "Food",
                "inStock": true
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/products/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Star Cat"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.category").value("Food"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void createProductShouldRejectInvalidName() throws Exception {
        String json = """
            {
                "name": "Dog",
                "description": "Food for cosmic cats",
                "price": 19.99,
                "category": "Food",
                "inStock": true
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductShouldRejectPriceBelowMinimum() throws Exception {
        String json = """
            {
                "name": "Star Cat",
                "description": "Food for cosmic cats",
                "price": 5.00,
                "category": "Food",
                "inStock": true
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductShouldReturnUpdatedProduct() throws Exception {
        when(productService.updateProduct(
                org.mockito.ArgumentMatchers.eq(1L),
                any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        String json = """
            {
                "id": 1,
                "name": "Comet Cat",
                "description": "Food for cosmic cats",
                "price": 25.00,
                "category": "Food",
                "inStock": true
            }
            """;

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Comet Cat"))
                .andExpect(jsonPath("$.price").value(25.00));
    }

    @Test
    void deleteProductShouldReturnNoContentWhenDeleted() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void deleteProductShouldReturnNotFoundProblem() throws Exception {
        when(productService.deleteProduct(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/not-found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/products/999"));
    }

    @Test
    void getProductByIdShouldReturnNotFoundProblem() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product with id 999 not found."));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Product with id 999 not found."));
    }

    @Test
    void createProductShouldReturnValidationProblem() throws Exception {
        String json = """
            {
                "name": "Star Cat",
                "description": "Food for cosmic cats",
                "price": 5.00,
                "category": "Food",
                "inStock": true
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/validation"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.instance").value("/api/v1/products"));
    }

    @Test
    void createProductShouldRejectMissingPrice() throws Exception {
        String json = """
            {
                "name": "Star Cat",
                "description": "Food for cosmic cats",
                "category": "Food"
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("price")));
    }

    @Test
    void createProductShouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\": \"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://cosmo-cats.market/errors/malformed-request"));
    }

    @Test
    void updateProductShouldRejectInvalidBody() throws Exception {
        String json = """
            {
                "name": "Star Cat",
                "description": "Food for cosmic cats",
                "price": -5,
                "category": "Food"
            }
            """;

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProductsShouldRejectNonNumericPage() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

}
