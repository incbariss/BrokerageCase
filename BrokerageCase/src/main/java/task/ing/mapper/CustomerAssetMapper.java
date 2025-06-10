package task.ing.mapper;

import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.dto.response.CustomerAssetsResponseDto;
import task.ing.model.entity.Customer;

import java.util.List;
import java.util.stream.Collectors;

public interface CustomerAssetMapper {

    static CustomerAssetsResponseDto toDto(Customer customer) {
        List<AssetResponseDto> assets = customer.getAssets().stream()
                .filter(asset -> !asset.isDeleted())
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());

        return new CustomerAssetsResponseDto(
                customer.getId(),
                customer.getName(),
                assets
        );
    }
}
