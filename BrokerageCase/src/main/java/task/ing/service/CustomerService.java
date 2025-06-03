package task.ing.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task.ing.mapper.CustomerMapper;
import task.ing.model.dto.request.CustomerRequestDto;
import task.ing.model.dto.response.CustomerResponseDto;
import task.ing.model.entity.Asset;
import task.ing.model.entity.AssetList;
import task.ing.model.entity.Customer;
import task.ing.repository.AssetListRepository;
import task.ing.repository.AssetRepository;
import task.ing.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final AssetListRepository assetListRepository;

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto dto) {
        if (customerRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }

        if (customerRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already registered");
        }

        Customer customer = CustomerMapper.toEntity(dto);
        Customer savedCustomer = customerRepository.save(customer);

        AssetList tryAssetList = assetListRepository.findByAssetName("TRY")
                .orElseThrow(() -> new RuntimeException("TRY asset not found in AssetList"));

        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(0.0);
        tryAsset.setUsableSize(0.0);
        tryAsset.setCustomer(savedCustomer);
        tryAsset.setAssetList(tryAssetList);

        assetRepository.save(tryAsset);

        return CustomerMapper.toDto(savedCustomer);

    }
}
