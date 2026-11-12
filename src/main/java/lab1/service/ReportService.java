package lab1.service;

import lab1.repository.OrderItemRepository;
import lab1.repository.ProductRepository;
import lab1.repository.projection.ProductSalesView;
import lab1.repository.projection.ProductSummaryView;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Звіти для Інтергалактичного комітету. */
@Service
@Transactional(readOnly = true)
public class ReportService {

    public static final int MAX_LIMIT = 100;

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public ReportService(OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    public List<ProductSalesView> getTopSellingProducts(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must be between 1 and " + MAX_LIMIT + ".");
        }
        return orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }

    public List<ProductSummaryView> getInStockProductsByCategory(String categoryName) {
        return productRepository.findInStockByCategory(categoryName);
    }
}
