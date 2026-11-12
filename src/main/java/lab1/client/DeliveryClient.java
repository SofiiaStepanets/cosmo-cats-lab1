package lab1.client;

import lab1.dto.DeliveryInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DeliveryClient {

    private final RestClient restClient;

    @Autowired
    public DeliveryClient(RestClient.Builder builder, @Value("${delivery.base-url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public DeliveryInfoDTO getDeliveryInfo(Long orderId) {
        return restClient.get()
                .uri("/api/delivery/{orderId}", orderId)
                .retrieve()
                .body(DeliveryInfoDTO.class);
    }
}
