package lab1.kafka;

import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.event.OrderPlacedEvent;
import lab1.producer.OrderProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Логіка побудови події без брокера: KafkaTemplate замінено заглушкою. */
class OrderProducerUnitTest {

    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private OrderProducer producer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        producer = new OrderProducer(kafkaTemplate, "order-placed");
    }

    private static Order order() {
        Product tuna = new Product();
        tuna.setName("Star Tuna");
        tuna.setPrice(new BigDecimal("25.50"));
        Product milk = new Product();
        milk.setName("Galaxy Milk");
        milk.setPrice(new BigDecimal("12.00"));

        Order order = new Order();
        order.setNaturalOrderId("order-7");
        order.setItems(List.of(new OrderItem(tuna, 2), new OrderItem(milk, 1)));
        return order;
    }

    @Test
    void sendOrderShouldBuildEventAndUseNaturalIdAsKey() {
        when(kafkaTemplate.send(eq("order-placed"), eq("order-7"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        CompletableFuture<?> future = producer.sendOrder(order(), List.of("Luna", "Comet"));

        ArgumentCaptor<OrderPlacedEvent> captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(kafkaTemplate).send(eq("order-placed"), eq("order-7"), captor.capture());
        OrderPlacedEvent event = captor.getValue();
        assertEquals("order-7", event.getNaturalOrderId().toString());
        assertEquals(63.00, event.getTotalAmount(), 0.001);
        assertEquals(2, event.getProductList().size());
        assertEquals("Star Tuna", event.getProductList().get(0).getName().toString());
        assertEquals(2, event.getProductList().get(0).getQuantity());
        assertEquals(2, event.getCatsNames().size());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void sendOrderShouldReturnFailedFutureWhenBrokerFails() {
        when(kafkaTemplate.send(eq("order-placed"), eq("order-7"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));

        CompletableFuture<?> future = producer.sendOrder(order(), List.of("Luna"));

        assertTrue(future.isCompletedExceptionally());
    }
}
