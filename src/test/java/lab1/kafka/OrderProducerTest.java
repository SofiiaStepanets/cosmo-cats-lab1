package lab1.kafka;

import java.math.BigDecimal;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.event.OrderItemEvent;
import lab1.event.OrderPlacedEvent;
import lab1.producer.OrderProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юніт-тест продюсера на вбудованій Kafka (@EmbeddedKafka).
 *
 * Піднімається лише частина контексту: OrderProducer + автоконфігурація Kafka.
 * База даних не потрібна. Замість справжнього Schema Registry — "mock://" реєстр у пам'яті.
 */
@SpringBootTest(
        classes = OrderProducer.class,
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.producer.properties.schema.registry.url=" + OrderProducerTest.MOCK_REGISTRY
        })
@ImportAutoConfiguration(KafkaAutoConfiguration.class)
@EmbeddedKafka(partitions = 1, topics = OrderProducerTest.TOPIC)
class OrderProducerTest {

    static final String TOPIC = "order-placed";
    static final String MOCK_REGISTRY = "mock://producer-test-registry";

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void sendOrderShouldPublishOrderPlacedEvent() {
        Order order = order("order-123",
                item("Star Tuna", 25.50, 2),
                item("Galaxy Milk", 12.00, 1));

        orderProducer.sendOrder(order, List.of("Luna", "Comet"));

        try (Consumer<String, OrderPlacedEvent> consumer = testConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, TOPIC);
            ConsumerRecord<String, OrderPlacedEvent> record =
                    KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(15));

            // ключ повідомлення — номер замовлення
            assertEquals("order-123", record.key());

            OrderPlacedEvent event = record.value();
            assertEquals("order-123", event.getNaturalOrderId());
            assertEquals(63.00, event.getTotalAmount(), 0.001); // 25.50 * 2 + 12.00
            assertEquals(List.of("Luna", "Comet"), event.getCatsNames());

            assertEquals(2, event.getProductList().size());
            OrderItemEvent first = event.getProductList().get(0);
            assertEquals("Star Tuna", first.getName());
            assertEquals(25.50, first.getPrice(), 0.001);
            assertEquals(2, first.getQuantity());
        }
    }

    /** Тестовий споживач, який читає подію з вбудованої Kafka і десеріалізує Avro. */
    private Consumer<String, OrderPlacedEvent> testConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "producer-test");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", MOCK_REGISTRY);
        props.put("specific.avro.reader", true);
        return new DefaultKafkaConsumerFactory<String, OrderPlacedEvent>(props).createConsumer();
    }

    private static OrderItem item(String productName, double price, int quantity) {
        Product product = new Product();
        product.setName(productName);
        product.setPrice(BigDecimal.valueOf(price));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private static Order order(String naturalId, OrderItem... items) {
        Order order = new Order();
        order.setNaturalOrderId(naturalId);
        order.setItems(List.of(items));
        return order;
    }
}
