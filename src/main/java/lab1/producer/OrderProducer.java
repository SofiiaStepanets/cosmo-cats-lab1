package lab1.producer;

import lab1.domain.Order;
import lab1.event.OrderItemEvent;
import lab1.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Публікує подію order-placed (Avro) у Kafka. Ключ повідомлення — natural id замовлення. */
@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String topicName;

    public OrderProducer(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
                         @Value("${app.kafka.topics.order-placed}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public CompletableFuture<SendResult<String, OrderPlacedEvent>> sendOrder(Order order, List<String> catsNames) {
        List<OrderItemEvent> items = order.getItems().stream()
                .map(item -> OrderItemEvent.newBuilder()
                        .setName(item.getProduct().getName())
                        .setPrice(item.getProduct().getPrice().doubleValue())
                        .setQuantity(item.getQuantity())
                        .build())
                .toList();

        OrderPlacedEvent event = OrderPlacedEvent.newBuilder()
                .setNaturalOrderId(order.getNaturalOrderId())
                .setProductList(items)
                .setTotalAmount(order.getTotalAmount().doubleValue())
                .setCatsNames(List.copyOf(catsNames))
                .build();

        CompletableFuture<SendResult<String, OrderPlacedEvent>> future =
                kafkaTemplate.send(topicName, order.getNaturalOrderId(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Order {} was not sent", order.getNaturalOrderId(), ex);
            } else {
                log.info("Order {} sent successfully", order.getNaturalOrderId());
            }
        });
        return future;
    }
}
