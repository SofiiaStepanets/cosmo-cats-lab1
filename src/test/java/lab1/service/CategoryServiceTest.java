package lab1.service;

import lab1.domain.Category;
import lab1.exception.ResourceNotFoundException;
import lab1.repository.CategoryRepository;
import lab1.repository.ProductRepository;
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

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategoryShouldIgnoreIdAndSave() {
        Category category = new Category(99L, "Food", "Space food");
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category created = categoryService.createCategory(category);

        assertNull(created.getId());
        verify(categoryRepository).save(category);
    }

    @Test
    void getAllCategoriesShouldReturnRepositoryResult() {
        List<Category> categories = List.of(new Category(1L, "Food", null));
        when(categoryRepository.findAll()).thenReturn(categories);

        assertEquals(categories, categoryService.getAllCategories());
    }

    @Test
    void getCategoryByIdShouldReturnCategory() {
        Category food = new Category(1L, "Food", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));

        assertSame(food, categoryService.getCategoryById(1L));
    }

    @Test
    void getCategoryByIdShouldThrowWhenNotFound() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(9L));
    }

    @Test
    void updateCategoryShouldUsePathId() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category updated = categoryService.updateCategory(1L, new Category(5L, "Toys", null));

        assertEquals(1L, updated.getId());
        assertEquals("Toys", updated.getName());
    }

    @Test
    void updateCategoryShouldThrowWhenNotFound() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(9L, new Category()));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategoryShouldNotDeleteWhenItHasProducts() {
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertFalse(categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategoryShouldDeleteEmptyCategory() {
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);
        when(categoryRepository.existsById(1L)).thenReturn(true);

        assertTrue(categoryService.deleteCategory(1L));
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategoryShouldReturnFalseWhenNotFound() {
        when(productRepository.existsByCategoryId(9L)).thenReturn(false);
        when(categoryRepository.existsById(9L)).thenReturn(false);

        assertFalse(categoryService.deleteCategory(9L));
    }
}
