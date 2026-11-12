package lab1.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Замовлення, отримане з Kafka (подія order-placed). */
@Entity
@Table(name = "ConsumedOrders")
public class ConsumedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConsOrdersId")
    private Long consOrderId;

    @NaturalId
    @Column(name = "CatsNaturalOrderId")
    private String catsNaturalOrderId;

    @Column(name = "TotalAmount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "CatsNames")
    private String catsNames;

    /** Заповнює БД (DEFAULT CURRENT_TIMESTAMP). */
    @Column(name = "WhenReceived", insertable = false, updatable = false)
    private LocalDateTime whenReceived;

    public Long getConsOrderId() {
        return consOrderId;
    }

    public void setConsOrderId(Long consOrderId) {
        this.consOrderId = consOrderId;
    }

    public String getCatsNaturalOrderId() {
        return catsNaturalOrderId;
    }

    public void setCatsNaturalOrderId(String catsNaturalOrderId) {
        this.catsNaturalOrderId = catsNaturalOrderId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCatsNames() {
        return catsNames;
    }

    public void setCatsNames(String catsNames) {
        this.catsNames = catsNames;
    }

    public LocalDateTime getWhenReceived() {
        return whenReceived;
    }

    public void setWhenReceived(LocalDateTime whenReceived) {
        this.whenReceived = whenReceived;
    }
}
