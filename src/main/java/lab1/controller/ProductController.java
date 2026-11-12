package lab1.controller;

import jakarta.validation.Valid;
import lab1.domain.Product;
import lab1.dto.PagedResponse;
import lab1.dto.ProductDetailsDTO;
import lab1.exception.ResourceNotFoundException;
import lab1.mapper.ProductMapper;
import lab1.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public PagedResponse<ProductDetailsDTO> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be >= 0 and size must be between 1 and " + MAX_PAGE_SIZE + "."
            );
        }

        List<ProductDetailsDTO> products = productService.getAllProducts()
                .stream()
                .map(productMapper::toDto)
                .toList();

        long totalElements = products.size();
        int fromIndex = (int) Math.min((long) page * size, totalElements);
        int toIndex = (int) Math.min((long) fromIndex + size, totalElements);

        return new PagedResponse<>(products.subList(fromIndex, toIndex), page, size, totalElements);
    }

    @GetMapping("/{id}")
    public ProductDetailsDTO getProductById(@PathVariable Long id) {
        return productMapper.toDto(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDetailsDTO> createProduct(@RequestBody @Valid ProductDetailsDTO dto) {
        Product created = productService.createProduct(productMapper.toDomain(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(productMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ProductDetailsDTO updateProduct(@PathVariable Long id, @RequestBody @Valid ProductDetailsDTO dto) {
        Product updated = productService.updateProduct(id, productMapper.toDomain(dto));
        return productMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productService.deleteProduct(id)) {
            throw new ResourceNotFoundException("Product with id " + id + " not found.");
        }
        return ResponseEntity.noContent().build();
    }
}
