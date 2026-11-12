package lab1.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import lab1.dto.DeliveryInfoDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

/** Bonus: stubbing of the 3rd-party delivery service with WireMock. */
class DeliveryClientWireMockTest {

    private WireMockServer wireMock;
    private DeliveryClient deliveryClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        deliveryClient = new DeliveryClient(RestClient.builder(), wireMock.baseUrl());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void shouldReturnDeliveryInfoFromStub() {
        wireMock.stubFor(get(urlEqualTo("/api/delivery/101"))
                .willReturn(okJson("""
                        {
                          "orderId": 101,
                          "status": "IN_TRANSIT",
                          "carrier": "Galaxy Express",
                          "estimatedDelivery": "2026-10-11"
                        }
                        """)));

        DeliveryInfoDTO info = deliveryClient.getDeliveryInfo(101L);

        assertEquals(101L, info.getOrderId());
        assertEquals("IN_TRANSIT", info.getStatus());
        assertEquals("Galaxy Express", info.getCarrier());
        assertEquals(LocalDate.of(2026, 10, 11), info.getEstimatedDelivery());
        wireMock.verify(1, getRequestedFor(urlEqualTo("/api/delivery/101")));
    }

    @Test
    void shouldThrowWhenStubReturnsNotFound() {
        wireMock.stubFor(get(urlEqualTo("/api/delivery/999")).willReturn(notFound()));

        assertThrows(HttpClientErrorException.NotFound.class, () -> deliveryClient.getDeliveryInfo(999L));
    }

    @Test
    void shouldThrowWhenStubReturnsServerError() {
        wireMock.stubFor(get(urlEqualTo("/api/delivery/500")).willReturn(serverError()));

        assertThrows(Exception.class, () -> deliveryClient.getDeliveryInfo(500L));
    }
}
