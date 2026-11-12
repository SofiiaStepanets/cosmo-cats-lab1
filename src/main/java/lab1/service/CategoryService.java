package lab1.service;

import lab1.domain.Category;
import lab1.exception.ResourceNotFoundException;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CategoryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(Category category) {
        category.setId(null);
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found."));
    }

    @Transactional
    public Category updateCategory(Long id, Category category) {
        getCategoryById(id);
        category.setId(id);
        return categoryRepository.save(category);
    }

    /** Категорію з товарами не видаляємо, щоб не втратити товари каскадом. */
    @Transactional
    public boolean deleteCategory(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            return false;
        }
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
