package task.ing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task.ing.mapper.AssetMapper;
import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.entity.Asset;
import task.ing.repository.AssetRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public List<AssetResponseDto> getAssetsByCustomerId(Long customerId) {
        List<Asset> assets = assetRepository.findByCustomerId(customerId);
        return assets.stream()
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());
    }
}
