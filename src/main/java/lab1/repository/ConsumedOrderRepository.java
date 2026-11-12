package lab1.repository;

import lab1.domain.ConsumedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedOrderRepository extends JpaRepository<ConsumedOrder, Long> {

    boolean existsByCatsNaturalOrderId(String catsNaturalOrderId);
}
