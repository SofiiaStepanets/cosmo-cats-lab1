package lab1.service;

import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.exception.ResourceNotFoundException;
import lab1.repository.OrderRepository;
import lab1.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /** Усі позиції зберігаються разом із замовленням або не зберігається нічого. */
    @Transactional
    public Order createOrder(Order order) {
        order.setId(null);
        for (OrderItem item : order.getItems()) {
            Long productId = item.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " not found."));
            item.setProduct(product);
            item.setOrder(order);
        }
        order.setNaturalOrderId(UUID.randomUUID().toString());
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " not found."));
    }

    public Order getOrderByNaturalId(String naturalOrderId) {
        return orderRepository.findByNaturalOrderId(naturalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + naturalOrderId + " not found."));
    }

    @Transactional
    public boolean deleteOrder(Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
