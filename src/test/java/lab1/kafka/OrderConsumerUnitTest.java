package lab1.kafka;

import lab1.consumer.OrderConsumer;
import lab1.domain.ConsumedOrder;
import lab1.event.OrderItemEvent;
import lab1.event.OrderPlacedEvent;
import lab1.repository.ConsumedOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Логіка consumeOrder без брокера: подію передаємо напряму. */
class OrderConsumerUnitTest {

    private ConsumedOrderRepository repository;
    private OrderConsumer consumer;

    @BeforeEach
    void setUp() {
        repository = mock(ConsumedOrderRepository.class);
        consumer = new OrderConsumer(repository);
    }

    private static OrderPlacedEvent event(String naturalId, double total) {
        return OrderPlacedEvent.newBuilder()
                .setNaturalOrderId(naturalId)
                .setProductList(List.of(
                        OrderItemEvent.newBuilder().setName("Star Tuna").setPrice(0.1).setQuantity(1).build(),
                        OrderItemEvent.newBuilder().setName("Galaxy Milk").setPrice(0.2).setQuantity(1).build()))
                .setTotalAmount(total)
                .setCatsNames(List.of("Luna", "Comet"))
                .build();
    }

    @Test
    void consumeOrderShouldSaveOrderWithRoundedAmount() {
        consumer.consumeOrder(event("order-1", 0.1 + 0.2)); // 0.30000000000000004 у double

        ArgumentCaptor<ConsumedOrder> captor = ArgumentCaptor.forClass(ConsumedOrder.class);
        verify(repository).save(captor.capture());
        ConsumedOrder saved = captor.getValue();
        assertEquals("order-1", saved.getCatsNaturalOrderId());
        assertEquals(new BigDecimal("0.30"), saved.getTotalAmount());
        assertEquals("Luna, Comet", saved.getCatsNames());
    }

    @Test
    void consumeOrderShouldSkipDuplicate() {
        when(repository.existsByCatsNaturalOrderId("order-2")).thenReturn(true);

        consumer.consumeOrder(event("order-2", 1.0));

        verify(repository, never()).save(any());
    }
}
