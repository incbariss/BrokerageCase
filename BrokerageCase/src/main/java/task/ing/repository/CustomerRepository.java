package task.ing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task.ing.model.entity.Customer;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Customer> findAllByIsDeletedFalse();

    Optional<Customer> findByIdAndIsDeletedFalse(Long id);

    Optional<Customer> findByUsername(String username);
}
