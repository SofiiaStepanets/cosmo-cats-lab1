package lab1.controller;

import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.exception.GlobalExceptionHandler;
import lab1.exception.ResourceNotFoundException;
import lab1.producer.OrderProducer;
import lab1.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private OrderService orderService;
    private OrderProducer orderProducer;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        orderProducer = mock(OrderProducer.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService, orderProducer))
                .setValidator(validator)
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
    void createOrderShouldSaveSendEventAndReturnCreated() throws Exception {
        Order saved = savedOrder();
        when(orderService.createOrder(any(Order.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": 1, "quantity": 2}], "catsNames": ["Luna", "Comet"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/orders/abc-123"))
                .andExpect(jsonPath("$.naturalOrderId").value("abc-123"));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).createOrder(captor.capture());
        assertEquals(1L, captor.getValue().getItems().get(0).getProduct().getId());
        assertEquals(2, captor.getValue().getItems().get(0).getQuantity());
        verify(orderProducer).sendOrder(saved, List.of("Luna", "Comet"));
    }

    @Test
    void createOrderShouldRejectMissingCatsNames() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": 1, "quantity": 2}], "catsNames": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        verifyNoInteractions(orderService, orderProducer);
    }

    @Test
    void createOrderShouldRejectZeroQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": 1, "quantity": 0}], "catsNames": ["Luna"]}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService, orderProducer);
    }

    @Test
    void createOrderShouldNotSendEventWhenProductNotFound() throws Exception {
        when(orderService.createOrder(any(Order.class)))
                .thenThrow(new ResourceNotFoundException("Product with id 9 not found."));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"productId": 9, "quantity": 1}], "catsNames": ["Luna"]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product with id 9 not found."));

        verify(orderProducer, never()).sendOrder(any(), anyList());
    }

    @Test
    void getOrderShouldReturnDetailsWithTotal() throws Exception {
        when(orderService.getOrderByNaturalId("abc-123")).thenReturn(savedOrder());

        mockMvc.perform(get("/api/v1/orders/abc-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naturalOrderId").value("abc-123"))
                .andExpect(jsonPath("$.items[0].productName").value("Star Tuna"))
                .andExpect(jsonPath("$.items[0].subtotal").value(39.98))
                .andExpect(jsonPath("$.totalAmount").value(39.98));
    }
}
