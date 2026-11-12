package lab1.integration;

import lab1.domain.ConsumedOrder;
import lab1.repository.ConsumedOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Таблиця ConsumedOrders (міграція 010) на справжній MySQL. */
class ConsumedOrderIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private ConsumedOrderRepository repository;

    @Autowired
    private TestEntityManager em;

    private static ConsumedOrder consumed(String naturalId) {
        ConsumedOrder order = new ConsumedOrder();
        order.setCatsNaturalOrderId(naturalId);
        order.setTotalAmount(new BigDecimal("63.00"));
        order.setCatsNames("Luna, Comet");
        return order;
    }

    @Test
    void saveShouldPersistAndFillReceivedTimeByDatabase() {
        ConsumedOrder saved = repository.save(consumed("order-1"));
        em.flush();
        em.clear();

        ConsumedOrder fromDb = repository.findById(saved.getConsOrderId()).orElseThrow();
        assertEquals(0, new BigDecimal("63.00").compareTo(fromDb.getTotalAmount()));
        assertEquals("Luna, Comet", fromDb.getCatsNames());
        assertNotNull(fromDb.getWhenReceived());
        assertTrue(repository.existsByCatsNaturalOrderId("order-1"));
        assertFalse(repository.existsByCatsNaturalOrderId("order-2"));
    }

    @Test
    void sameNaturalIdTwiceShouldViolateUniqueConstraint() {
        repository.saveAndFlush(consumed("order-1"));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(consumed("order-1")));
    }
}
