package lab1.controller;

import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.exception.GlobalExceptionHandler;
import lab1.exception.ResourceNotFoundException;
import lab1.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private OrderService orderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static Order savedOrder() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Star Tuna");
        product.setPrice(new BigDecimal("19.99"));

        Order order = new Order();
        order.setNaturalOrderId("abc-123");
        order.setItems(List.of(new OrderItem(product, 2)));
        return order;
    }

    @Test
    void createOrderShouldReturnCreatedWithNaturalId() throws Exception {
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"product": {"id": 1}, "quantity": 2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/orders/abc-123"))
                .andExpect(jsonPath("$.naturalOrderId").value("abc-123"));
    }

    @Test
    void createOrderShouldReturnNotFoundForUnknownProduct() throws Exception {
        when(orderService.createOrder(any(Order.class)))
                .thenThrow(new ResourceNotFoundException("Product with id 9 not found."));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"product": {"id": 9}, "quantity": 1}]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product with id 9 not found."));
    }

    @Test
    void getOrderShouldReturnDetailsWithTotal() throws Exception {
        when(orderService.getOrderByNaturalId("abc-123")).thenReturn(savedOrder());

        mockMvc.perform(get("/api/v1/orders/abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naturalOrderId").value("abc-123"))
                .andExpect(jsonPath("$.items[0].productName").value("Star Tuna"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(39.98))
                .andExpect(jsonPath("$.totalAmount").value(39.98));
    }
}
