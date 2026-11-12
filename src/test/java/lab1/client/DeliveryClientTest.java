
package lab1.client;

import lab1.client.DeliveryClient;
import lab1.dto.DeliveryInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class DeliveryClientTest {

    private MockRestServiceServer server;
    private DeliveryClient deliveryClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        deliveryClient = new DeliveryClient(
                builder, "http://localhost:8081"
        );
    }

    @Test
    void shouldReturnDeliveryInfo() {
        server.expect(requestTo(
                        "http://localhost:8081/api/delivery/123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "orderId": 123,
                          "status": "SHIPPED",
                          "carrier": "DHL",
                          "estimatedDelivery": "2026-10-15"
                        }
                        """, MediaType.APPLICATION_JSON));

        DeliveryInfoDTO result = deliveryClient.getDeliveryInfo(123L);

        assertNotNull(result);
        assertEquals(123L, result.getOrderId());
        assertEquals("SHIPPED", result.getStatus());
        assertEquals("DHL", result.getCarrier());
        assertEquals(
                LocalDate.of(2026, 10, 15),
                result.getEstimatedDelivery()
        );

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenDeliveryNotFound() {
        server.expect(requestTo(
                        "http://localhost:8081/api/delivery/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(
                HttpClientErrorException.class,
                () -> deliveryClient.getDeliveryInfo(999L)
        );

        server.verify();
    }
}
