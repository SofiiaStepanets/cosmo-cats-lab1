package lab1.mapper;

import lab1.domain.Category;
import lab1.domain.Product;
import lab1.dto.ProductDetailsDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDetailsDTO toDto(Product product) {
        ProductDetailsDTO dto = new ProductDetailsDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
        dto.setInStock(product.isInStock());
        return dto;
    }

    /** id навмисно не копіюється: його задає сервер (POST) або шлях запиту (PUT). */
    public Product toDomain(ProductDetailsDTO dto) {
        Category category = new Category();
        category.setName(dto.getCategory());

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);
        product.setInStock(dto.isInStock());
        return product;
    }
}
