package task.ing.mapper;

import task.ing.model.dto.AssetDto;
import task.ing.model.entity.Asset;

public interface AssetMapper {

    static AssetDto toDto(Asset asset) {
        return new AssetDto(
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize(),
                asset.getAssetList().getCurrentPrice()

        );
    }
}
