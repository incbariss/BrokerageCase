package task.ing.repository;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import task.ing.model.entity.Order;
import task.ing.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByOrderStatus(OrderStatus status);
}
