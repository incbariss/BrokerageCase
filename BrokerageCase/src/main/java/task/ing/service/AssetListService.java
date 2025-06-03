package task.ing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task.ing.mapper.AssetListMapper;
import task.ing.model.dto.response.AssetListResponseDto;
import task.ing.repository.AssetListRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetListService {

    private final AssetListRepository assetListRepository;

    public List<AssetListResponseDto> getAssetList() {
        return assetListRepository.findAll()
                .stream()
                .map(AssetListMapper::toDto)
                .collect(Collectors.toList());
    }
}
