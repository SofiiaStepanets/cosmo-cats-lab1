package lab1.controller;

import lab1.repository.projection.ProductSalesView;
import lab1.repository.projection.ProductSummaryView;
import lab1.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/top-products")
    public List<ProductSalesView> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return reportService.getTopSellingProducts(limit);
    }

    @GetMapping("/categories/{categoryName}/in-stock")
    public List<ProductSummaryView> inStockByCategory(@PathVariable String categoryName) {
        return reportService.getInStockProductsByCategory(categoryName);
    }
}
