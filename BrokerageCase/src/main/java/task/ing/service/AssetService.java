package task.ing.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task.ing.mapper.AssetMapper;
import task.ing.mapper.CustomerAssetMapper;
import task.ing.model.dto.request.AssetCreateRequestDto;
import task.ing.model.dto.request.AssetUpdateRequestDto;
import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.dto.response.CustomerAssetsResponseDto;
import task.ing.model.entity.Asset;
import task.ing.model.entity.AssetList;
import task.ing.model.entity.Customer;
import task.ing.model.enums.Role;
import task.ing.repository.AssetListRepository;
import task.ing.repository.AssetRepository;
import task.ing.repository.CustomerRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetListRepository assetListRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public List<AssetResponseDto> getAssetsByCustomerId(Long customerId, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Customer targetCustomer = customerRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new RuntimeException("Target customer not found or deleted"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(targetCustomer.getId())) {
            throw new RuntimeException("You are not authorized to view these assets");
        }

        List<Asset> assets = assetRepository.findByCustomerIdAndIsDeletedFalse(customerId);
        return assets.stream()
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());
    }


    public List<CustomerAssetsResponseDto> getAllAssetsGroupedByCustomer() {
        List<Customer> customers = customerRepository.findAllByIsDeletedFalse();
        return customers.stream()
                .map(CustomerAssetMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public AssetResponseDto updateAsset(Long assetId, AssetUpdateRequestDto dto) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        asset.setSize(dto.size());
        asset.setUsableSize(dto.usableSize());

        return AssetMapper.toDto(assetRepository.save(asset));
    }

    public AssetResponseDto addAssetToCustomer(AssetCreateRequestDto dto) {
        AssetList assetList = assetListRepository.findById(dto.assetListId())
                .orElseThrow(() -> new NoSuchElementException("Asset not found"));

        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        Asset asset = new Asset();
        asset.setAssetName(assetList.getAssetName());
        asset.setSize(dto.size());
        asset.setUsableSize(dto.size());
        asset.setAssetList(assetList);
        asset.setCustomer(customer);

        Asset saved = assetRepository.save(asset);
        return AssetMapper.toDto(saved);
    }

    public void softDeleteAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NoSuchElementException("Asset not found"));

        asset.setDeleted(true);
        assetRepository.save(asset);
    }

    public void restoreAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NoSuchElementException("Asset not found"));

        asset.setDeleted(false);
        assetRepository.save(asset);
    }


}
