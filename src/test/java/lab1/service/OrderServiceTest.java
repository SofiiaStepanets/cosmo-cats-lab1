package lab1.service;

import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.exception.ResourceNotFoundException;
import lab1.repository.OrderRepository;
import lab1.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private static Product productRef(Long id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private static Order order(OrderItem... items) {
        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(items)));
        return order;
    }

    @Test
    void createOrderShouldLoadProductsLinkItemsAndGenerateNaturalId() {
        Product starTuna = productRef(1L);
        starTuna.setName("Star Tuna");
        starTuna.setPrice(new BigDecimal("19.99"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(starTuna));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = order(new OrderItem(productRef(1L), 2));
        order.setId(42L);
        Order created = orderService.createOrder(order);

        assertNull(created.getId());
        assertDoesNotThrow(() -> UUID.fromString(created.getNaturalOrderId()));
        OrderItem item = created.getItems().get(0);
        assertSame(starTuna, item.getProduct());
        assertSame(created, item.getOrder());
        assertEquals(new BigDecimal("39.98"), created.getTotalAmount());
    }

    @Test
    void createOrderShouldThrowWhenProductNotFound() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(order(new OrderItem(productRef(9L), 1))));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrdersShouldReturnRepositoryResult() {
        List<Order> orders = List.of(new Order());
        when(orderRepository.findAll()).thenReturn(orders);

        assertEquals(orders, orderService.getAllOrders());
    }

    @Test
    void getOrderByIdShouldReturnOrThrow() {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findById(9L)).thenReturn(Optional.empty());

        assertSame(order, orderService.getOrderById(1L));
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(9L));
    }

    @Test
    void getOrderByNaturalIdShouldReturnOrThrow() {
        Order order = new Order();
        when(orderRepository.findByNaturalOrderId("abc")).thenReturn(Optional.of(order));
        when(orderRepository.findByNaturalOrderId("nope")).thenReturn(Optional.empty());

        assertSame(order, orderService.getOrderByNaturalId("abc"));
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderByNaturalId("nope"));
    }

    @Test
    void deleteOrderShouldDeleteWhenExists() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        assertTrue(orderService.deleteOrder(1L));
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrderShouldReturnFalseWhenNotFound() {
        when(orderRepository.existsById(9L)).thenReturn(false);

        assertFalse(orderService.deleteOrder(9L));
        verify(orderRepository, never()).deleteById(any());
    }
}
