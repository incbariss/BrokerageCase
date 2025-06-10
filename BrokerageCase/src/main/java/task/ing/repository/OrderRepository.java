package task.ing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task.ing.model.entity.Order;
import task.ing.model.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdAndCreatedDateBetween(Long customerId, LocalDate startDate, LocalDate endDate);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByOrderStatus(OrderStatus status);
}
