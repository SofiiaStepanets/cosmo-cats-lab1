package lab1.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailsDTO(String naturalOrderId, List<Item> items, BigDecimal totalAmount) {

    public record Item(Long productId, String productName, int quantity, BigDecimal subtotal) {
    }
}
