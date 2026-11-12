package lab1.repository.projection;

/** Projection для звіту комітету: лише id, назва товару та скільки штук продано. */
public interface ProductSalesView {

    Long getProductId();

    String getProductName();

    Long getTotalQuantity();
}
