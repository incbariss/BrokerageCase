package task.ing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task.ing.model.entity.AssetList;

import java.util.Optional;

@Repository
public interface AssetListRepository extends JpaRepository<AssetList, Long> {

    Optional<AssetList> findByAssetName(String assetName);
}
