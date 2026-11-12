package lab1.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    private static Product product(String price) {
        Product product = new Product();
        product.setName("Star Tuna");
        product.setPrice(new BigDecimal(price));
        return product;
    }

    @Test
    void orderItemSubtotalShouldBePriceTimesQuantity() {
        OrderItem item = new OrderItem(product("19.99"), 3);

        assertEquals(new BigDecimal("59.97"), item.getSubtotal());
    }

    @Test
    void orderTotalShouldSumAllItemsWithoutPrecisionLoss() {
        Order order = new Order();
        order.setId(1L);
        order.setItems(List.of(
                new OrderItem(product("0.10"), 1),
                new OrderItem(product("0.20"), 1)));

        assertEquals(1L, order.getId());
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("0.30"), order.getTotalAmount());
    }

    @Test
    void emptyOrderTotalShouldBeZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(new Order().getTotalAmount()));
    }

    @Test
    void orderItemSettersShouldUpdateFields() {
        OrderItem item = new OrderItem();
        Product product = product("12.50");
        item.setProduct(product);
        item.setQuantity(2);

        assertSame(product, item.getProduct());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void cartShouldHoldItems() {
        Product product = product("12.50");
        CartItem item = new CartItem(product, 2);

        Cart cart = new Cart();
        cart.setId(7L);
        cart.getItems().add(item);

        assertEquals(7L, cart.getId());
        assertEquals(1, cart.getItems().size());
        assertSame(product, cart.getItems().get(0).getProduct());
        assertEquals(2, cart.getItems().get(0).getQuantity());

        CartItem other = new CartItem();
        other.setProduct(product);
        other.setQuantity(5);
        cart.setItems(List.of(other));
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void categoryShouldBeActiveByDefaultAndKeepFields() {
        Category category = new Category(1L, "Food", "Space food");

        assertTrue(category.isActive());
        assertEquals(1L, category.getId());
        assertEquals("Food", category.getName());
        assertEquals("Space food", category.getDescription());

        category.setActive(false);
        category.setDescription("Changed");
        assertFalse(category.isActive());
        assertEquals("Changed", category.getDescription());
    }
}
