package lab1.controller;

import lab1.client.DeliveryClient;
import lab1.dto.DeliveryInfoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {
    private final DeliveryClient deliveryClient;

    public DeliveryController(DeliveryClient deliveryClient) {
        this.deliveryClient = deliveryClient;
    }
    @GetMapping("/{orderId}")
    public DeliveryInfoDTO getDeliveryInfo(@PathVariable Long orderId){
        return deliveryClient.getDeliveryInfo(orderId);
    }
}
