package task.ing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task.ing.mapper.AssetListMapper;
import task.ing.model.dto.request.AssetListRequestDto;
import task.ing.model.dto.response.AssetListResponseDto;
import task.ing.model.entity.AssetList;
import task.ing.repository.AssetListRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetListService {

    private final AssetListRepository assetListRepository;

    public List<AssetListResponseDto> getAssetList() {
        return assetListRepository.findAll()
                .stream()
                .filter(asset -> !asset.isDeleted())
                .map(AssetListMapper::toDto)
                .collect(Collectors.toList());
    }

    public AssetListResponseDto addAsset(AssetListRequestDto dto) {
        assetListRepository.findByAssetName(dto.assetName())
                .ifPresent(asset -> {
                    throw new IllegalArgumentException("Asset with this name already exist.");
                });

        AssetList asset = AssetListMapper.toEntity(dto);
        AssetList savedAsset = assetListRepository.save(asset);
        return AssetListMapper.toDto(savedAsset);
    }

    public AssetListResponseDto updateAsset(Long id, AssetListRequestDto requestDto) {
        AssetList asset = assetListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asset not found"));

        asset.setAssetName(requestDto.assetName());
        asset.setAssetFullName(requestDto.assetFullName());
        asset.setCurrentPrice(requestDto.currentPrice());

        AssetList updated = assetListRepository.save(asset);
        return AssetListMapper.toDto(updated);
    }


    public void softDeleteAsset(Long id) {
        AssetList asset = assetListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asset not found"));

        asset.setDeleted(true);
        assetListRepository.save(asset);
    }

    public void restoreAsset(Long id) {
        AssetList asset = assetListRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asset not found"));

        asset.setDeleted(false);
        assetListRepository.save(asset);
    }
}
