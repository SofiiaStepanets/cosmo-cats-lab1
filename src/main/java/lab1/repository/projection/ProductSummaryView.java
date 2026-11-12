package lab1.repository.projection;

import java.math.BigDecimal;

/** Projection з коротким описом товару без опису, категорії та інших полів. */
public interface ProductSummaryView {

    Long getId();

    String getName();

    BigDecimal getPrice();
}
