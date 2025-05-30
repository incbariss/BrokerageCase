package task.ing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task.ing.model.entity.AssetList;

import java.util.Optional;

public interface AssetListRepository extends JpaRepository<AssetList, Long> {

    Optional<AssetList> findByShortName(String shortName);
}
