package lab1.consumer;

import lab1.domain.ConsumedOrder;
import lab1.event.OrderPlacedEvent;
import lab1.repository.ConsumedOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Отримує order-placed і зберігає замовлення в БД. Повторна доставка не створює дублікат. */
@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final ConsumedOrderRepository consumedOrderRepository;

    public OrderConsumer(ConsumedOrderRepository consumedOrderRepository) {
        this.consumedOrderRepository = consumedOrderRepository;
    }

    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.order-placed}")
    public void consumeOrder(OrderPlacedEvent event) {
        String naturalOrderId = event.getNaturalOrderId().toString();
        if (consumedOrderRepository.existsByCatsNaturalOrderId(naturalOrderId)) {
            log.info("Order {} is a duplicate, already saved", naturalOrderId);
            return;
        }

        ConsumedOrder consumedOrder = new ConsumedOrder();
        consumedOrder.setCatsNaturalOrderId(naturalOrderId);
        consumedOrder.setTotalAmount(BigDecimal.valueOf(event.getTotalAmount()).setScale(2, RoundingMode.HALF_UP));
        consumedOrder.setCatsNames(String.join(", ", event.getCatsNames()));
        consumedOrderRepository.save(consumedOrder);
        log.info("Order {} processed successfully", naturalOrderId);
    }
}
