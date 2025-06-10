package task.ing.mapper;

import task.ing.model.dto.request.AssetListRequestDto;
import task.ing.model.dto.response.AssetListResponseDto;
import task.ing.model.entity.AssetList;

public interface AssetListMapper {

    static AssetList toEntity(AssetListRequestDto dto) {
        AssetList assetList = new AssetList();
        assetList.setAssetName(dto.assetName());
        assetList.setAssetFullName(dto.assetFullName());
        assetList.setCurrentPrice(dto.currentPrice());
        return assetList;
    }

    static AssetListResponseDto toDto(AssetList assetList) {
        return new AssetListResponseDto(
                assetList.getId(),
                assetList.getAssetName(),
                assetList.getAssetFullName(),
                assetList.getCurrentPrice()
        );
    }
}
