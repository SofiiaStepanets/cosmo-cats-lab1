package lab1.kafka;

import lab1.consumer.OrderConsumer;
import lab1.domain.ConsumedOrder;
import lab1.event.OrderItemEvent;
import lab1.event.OrderPlacedEvent;
import lab1.repository.ConsumedOrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Юніт-тест споживача на вбудованій Kafka (@EmbeddedKafka).
 *
 * Тест відправляє подію у вбудовану Kafka, а @KafkaListener у OrderConsumer її отримує.
 * Репозиторій замінено заглушкою (@MockitoBean), тому база даних не потрібна:
 * перевіряємо, що споживач викликав save() з правильними даними.
 */
@SpringBootTest(
        classes = OrderConsumer.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.producer.properties.schema.registry.url=" + OrderConsumerTest.MOCK_REGISTRY,
                "spring.kafka.consumer.properties.schema.registry.url=" + OrderConsumerTest.MOCK_REGISTRY
        })
@ImportAutoConfiguration(KafkaAutoConfiguration.class)
@EmbeddedKafka(partitions = 1, topics = OrderConsumerTest.TOPIC)
class OrderConsumerTest {

    static final String TOPIC = "order-placed";
    static final String MOCK_REGISTRY = "mock://consumer-test-registry";

    /** Час очікування, поки слухач у фоновому потоці обробить повідомлення. */
    private static final int WAIT_MS = 15_000;

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @MockitoBean
    private ConsumedOrderRepository consumedOrderRepository;

    @Test
    void consumeOrderShouldSaveReceivedOrder() {
        OrderPlacedEvent event = event("order-1");

        kafkaTemplate.send(TOPIC, event.getNaturalOrderId(), event);

        ArgumentCaptor<ConsumedOrder> captor = ArgumentCaptor.forClass(ConsumedOrder.class);
        verify(consumedOrderRepository, timeout(WAIT_MS)).save(captor.capture());

        ConsumedOrder saved = captor.getValue();
        assertEquals("order-1", saved.getCatsNaturalOrderId());
        assertEquals(new java.math.BigDecimal("63.00"), saved.getTotalAmount());
        assertEquals("Luna, Comet", saved.getCatsNames());
    }

    @Test
    void consumeOrderShouldSkipDuplicate() {
        when(consumedOrderRepository.existsByCatsNaturalOrderId("order-2")).thenReturn(true);
        OrderPlacedEvent event = event("order-2");

        kafkaTemplate.send(TOPIC, event.getNaturalOrderId(), event);

        verify(consumedOrderRepository, timeout(WAIT_MS)).existsByCatsNaturalOrderId("order-2");
        verify(consumedOrderRepository, never()).save(any());
    }

    private static OrderPlacedEvent event(String naturalId) {
        return OrderPlacedEvent.newBuilder()
                .setNaturalOrderId(naturalId)
                .setProductList(List.of(
                        OrderItemEvent.newBuilder().setName("Star Tuna").setPrice(25.50).setQuantity(2).build(),
                        OrderItemEvent.newBuilder().setName("Galaxy Milk").setPrice(12.00).setQuantity(1).build()))
                .setTotalAmount(63.00)
                .setCatsNames(List.of("Luna", "Comet"))
                .build();
    }
}
