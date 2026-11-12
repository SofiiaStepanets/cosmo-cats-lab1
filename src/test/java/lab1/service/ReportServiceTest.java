package lab1.service;

import lab1.repository.OrderItemRepository;
import lab1.repository.ProductRepository;
import lab1.repository.projection.ProductSalesView;
import lab1.repository.projection.ProductSummaryView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void topSellingProductsShouldUseLimitAsPageSize() {
        List<ProductSalesView> report = List.of(mock(ProductSalesView.class));
        when(orderItemRepository.findTopSellingProducts(PageRequest.of(0, 3))).thenReturn(report);

        assertSame(report, reportService.getTopSellingProducts(3));
    }

    @Test
    void topSellingProductsShouldRejectInvalidLimit() {
        assertThrows(IllegalArgumentException.class, () -> reportService.getTopSellingProducts(0));
        assertThrows(IllegalArgumentException.class,
                () -> reportService.getTopSellingProducts(ReportService.MAX_LIMIT + 1));
        verify(orderItemRepository, never()).findTopSellingProducts(any());
    }

    @Test
    void inStockProductsByCategoryShouldDelegateToRepository() {
        List<ProductSummaryView> products = List.of(mock(ProductSummaryView.class));
        when(productRepository.findInStockByCategory("Food")).thenReturn(products);

        assertSame(products, reportService.getInStockProductsByCategory("Food"));
    }
}
