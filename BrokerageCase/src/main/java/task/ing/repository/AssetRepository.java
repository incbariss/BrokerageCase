package task.ing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task.ing.model.entity.Asset;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByCustomerId(Long customerId);

    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);

    List<Asset> findByCustomerIdAndIsDeletedFalse(Long customerId);

    List<Asset> findByIsDeletedFalse();

}
