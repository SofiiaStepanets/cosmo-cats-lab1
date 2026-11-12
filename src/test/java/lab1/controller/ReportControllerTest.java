package lab1.controller;

import lab1.exception.GlobalExceptionHandler;
import lab1.repository.projection.ProductSalesView;
import lab1.repository.projection.ProductSummaryView;
import lab1.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest {

    private ReportService reportService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    record Sales(Long productId, String productName, Long totalQuantity) implements ProductSalesView {
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Long getTotalQuantity() { return totalQuantity; }
    }

    record Summary(Long id, String name, BigDecimal price) implements ProductSummaryView {
        public Long getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
    }

    @Test
    void topProductsShouldReturnReport() throws Exception {
        when(reportService.getTopSellingProducts(2)).thenReturn(List.of(
                new Sales(1L, "Star Tuna", 10L),
                new Sales(2L, "Galaxy Milk", 4L)));

        mockMvc.perform(get("/api/v1/reports/top-products").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Star Tuna"))
                .andExpect(jsonPath("$[0].totalQuantity").value(10))
                .andExpect(jsonPath("$[1].productName").value("Galaxy Milk"));
    }

    @Test
    void topProductsShouldReturnBadRequestForInvalidLimit() throws Exception {
        when(reportService.getTopSellingProducts(0))
                .thenThrow(new IllegalArgumentException("Limit must be between 1 and 100."));

        mockMvc.perform(get("/api/v1/reports/top-products").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Limit must be between 1 and 100."));
    }

    @Test
    void inStockByCategoryShouldReturnSummaries() throws Exception {
        when(reportService.getInStockProductsByCategory("Food"))
                .thenReturn(List.of(new Summary(1L, "Star Tuna", new BigDecimal("19.99"))));

        mockMvc.perform(get("/api/v1/reports/categories/Food/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Star Tuna"))
                .andExpect(jsonPath("$[0].price").value(19.99));
    }
}
