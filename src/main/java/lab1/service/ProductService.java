package lab1.service;

import lab1.domain.Category;
import lab1.domain.Product;
import lab1.exception.ResourceNotFoundException;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Product createProduct(Product product) {
        product.setId(null);
        product.setCategory(findCategory(product.getCategory().getName()));
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found."));
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        getProductById(id);
        product.setCategory(findCategory(product.getCategory().getName()));
        product.setId(id);
        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Category findCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category '" + name + "' not found."));
    }
}
