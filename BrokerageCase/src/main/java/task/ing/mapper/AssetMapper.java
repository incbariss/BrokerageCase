package task.ing.mapper;

import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.entity.Asset;

public interface AssetMapper {

    static AssetResponseDto toDto(Asset asset) {
        return new AssetResponseDto(
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize(),
                asset.getAssetList().getCurrentPrice()

        );
    }
}
