package lab1.controller;

import jakarta.validation.Valid;
import lab1.domain.Order;
import lab1.domain.OrderItem;
import lab1.domain.Product;
import lab1.dto.OrderDetailsDTO;
import lab1.dto.PlaceOrderRequest;
import lab1.producer.OrderProducer;
import lab1.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderProducer orderProducer;

    public OrderController(OrderService orderService, OrderProducer orderProducer) {
        this.orderService = orderService;
        this.orderProducer = orderProducer;
    }

    /** Зберігає замовлення в БД (транзакція), після коміту публікує подію order-placed у Kafka. */
    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody @Valid PlaceOrderRequest request) {
        Order saved = orderService.createOrder(toOrder(request));
        orderProducer.sendOrder(saved, request.catsNames());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{naturalOrderId}")
                .buildAndExpand(saved.getNaturalOrderId())
                .toUri();
        return ResponseEntity.created(location).body(Map.of("naturalOrderId", saved.getNaturalOrderId()));
    }

    @GetMapping("/{naturalOrderId}")
    public OrderDetailsDTO getOrder(@PathVariable String naturalOrderId) {
        Order order = orderService.getOrderByNaturalId(naturalOrderId);
        return new OrderDetailsDTO(
                order.getNaturalOrderId(),
                order.getItems().stream()
                        .map(item -> new OrderDetailsDTO.Item(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getSubtotal()))
                        .toList(),
                order.getTotalAmount());
    }

    private static Order toOrder(PlaceOrderRequest request) {
        Order order = new Order();
        order.setItems(new ArrayList<>());
        for (PlaceOrderRequest.Item item : request.items()) {
            Product product = new Product();
            product.setId(item.productId());
            order.getItems().add(new OrderItem(product, item.quantity()));
        }
        return order;
    }
}
