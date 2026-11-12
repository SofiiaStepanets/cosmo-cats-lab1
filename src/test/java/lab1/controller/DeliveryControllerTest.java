package lab1.controller;

import lab1.client.DeliveryClient;
import lab1.controller.DeliveryController;
import lab1.dto.DeliveryInfoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeliveryControllerTest {

    @Test
    void getDeliveryInfoShouldReturnInfoFromClient() throws Exception {
        DeliveryClient client = mock(DeliveryClient.class);

        DeliveryInfoDTO info = new DeliveryInfoDTO();
        info.setOrderId(123L);
        info.setStatus("SHIPPED");
        info.setCarrier("DHL");
        when(client.getDeliveryInfo(123L)).thenReturn(info);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DeliveryController(client)).build();

        mockMvc.perform(get("/api/v1/delivery/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(123))
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.carrier").value("DHL"));
    }
}
