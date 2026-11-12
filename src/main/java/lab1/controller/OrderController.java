package lab1.controller;

import lab1.domain.Order;
import lab1.dto.OrderDetailsDTO;
import lab1.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Order order) {
        Order saved = orderService.createOrder(order);
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
}
