package task.ing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task.ing.model.entity.Asset;

import java.util.List;

public interface AssetRepository extends JpaRepository <Asset, Long> {

    List<Asset> findByCustomerId(Long customerId);
}
