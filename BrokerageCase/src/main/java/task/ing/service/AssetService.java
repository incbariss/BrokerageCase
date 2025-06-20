package task.ing.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import task.ing.mapper.AssetMapper;
import task.ing.mapper.CustomerAssetMapper;
import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.dto.response.CustomerAssetsResponseDto;
import task.ing.model.entity.Asset;
import task.ing.model.entity.Customer;
import task.ing.model.enums.Role;
import task.ing.repository.AssetListRepository;
import task.ing.repository.AssetRepository;
import task.ing.repository.CustomerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetListRepository assetListRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public List<AssetResponseDto> getAssetsForCurrentUser(String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        List<Asset> assets = assetRepository.findByCustomerIdAndIsDeletedFalse(currentUser.getId());
        return assets.stream()
                .map(AssetMapper::toDto)
                .toList();
    }


    public List<CustomerAssetsResponseDto> adminGetAllAssetsGroupedByCustomer() {
        List<Customer> customers = customerRepository.findAllByIsDeletedFalse();
        return customers.stream()
                .map(CustomerAssetMapper::toDto)
                .toList();
    }


}
