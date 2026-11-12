package lab1.service;

import lab1.domain.Category;
import lab1.domain.Product;
import lab1.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CRUD над mock-даними в пам'яті. На DB-тижні сховище заміниться на репозиторій,
 * а контракт сервісу залишиться тим самим.
 */
@Service
public class ProductService {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Category> categories = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public ProductService() {
        addCategory(new Category(1L, "Food", "Space food for cats"));
        addCategory(new Category(2L, "Toys", "Anti-gravity toys"));
        addCategory(new Category(3L, "Gadgets", "Cosmic gadgets"));

        createProduct(mock("Star Tuna", "Freeze-dried tuna for cats in orbit", "19.99", "Food"));
        createProduct(mock("Galaxy Milk", "Cosmic milk from the Milky Way", "12.50", "Food"));
        createProduct(mock("Comet Yarn Ball", "Anti-gravity yarn ball", "24.00", "Toys"));
    }

    public Product createProduct(Product product) {
        product.setId(idSequence.incrementAndGet());
        product.setCategory(findCategory(product.getCategory().getName()));
        products.put(product.getId(), product);
        return product;
    }

    public List<Product> getAllProducts() {
        List<Product> result = new ArrayList<>(products.values());
        result.sort(Comparator.comparing(Product::getId));
        return result;
    }

    public Product getProductById(Long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product with id " + id + " not found.");
        }
        return product;
    }

    public Product updateProduct(Long id, Product product) {
        getProductById(id);
        product.setCategory(findCategory(product.getCategory().getName()));
        product.setId(id);
        products.put(id, product);
        return product;
    }

    public boolean deleteProduct(Long id) {
        return products.remove(id) != null;
    }

    private Category findCategory(String name) {
        Category category = name == null ? null : categories.get(name.toLowerCase());
        if (category == null) {
            throw new ResourceNotFoundException("Category '" + name + "' not found.");
        }
        return category;
    }

    private void addCategory(Category category) {
        categories.put(category.getName().toLowerCase(), category);
    }

    private static Product mock(String name, String description, String price, String categoryName) {
        Category category = new Category();
        category.setName(categoryName);

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setCategory(category);
        product.setInStock(true);
        return product;
    }
}
