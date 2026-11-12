package lab1.repository;

import lab1.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByNaturalOrderId(String naturalOrderId);
}
