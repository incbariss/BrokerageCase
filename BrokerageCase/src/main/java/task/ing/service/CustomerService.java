package task.ing.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import task.ing.exceptions.AssetNotFoundException;
import task.ing.exceptions.CustomerNotFoundException;
import task.ing.exceptions.EmailAlreadyExistsException;
import task.ing.exceptions.UsernameAlreadyExistsException;
import task.ing.mapper.CustomerMapper;
import task.ing.model.dto.request.CustomerRequestDto;
import task.ing.model.dto.response.CustomerResponseDto;
import task.ing.model.dto.response.LoginResponseDto;
import task.ing.model.entity.Asset;
import task.ing.model.entity.AssetList;
import task.ing.model.entity.Customer;
import task.ing.model.enums.Role;
import task.ing.repository.AssetListRepository;
import task.ing.repository.AssetRepository;
import task.ing.repository.CustomerRepository;
import task.ing.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final AssetListRepository assetListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto dto) {
        if (customerRepository.existsByUsername(dto.username())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (customerRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        Customer customer = CustomerMapper.toEntity(dto);

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setRole(Role.ROLE_USER);

        Customer savedCustomer = customerRepository.save(customer);

        AssetList tryAssetList = assetListRepository.findByAssetName("TRY")
                .orElseThrow(() -> new AssetNotFoundException("TRY asset not found in AssetList"));

        Asset tryAsset = new Asset();
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(0.0);
        tryAsset.setUsableSize(0.0);
        tryAsset.setCustomer(savedCustomer);
        tryAsset.setAssetList(tryAssetList);

        assetRepository.save(tryAsset);

        return CustomerMapper.toDto(savedCustomer);
    }

    public LoginResponseDto login(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(customer.getUsername(), customer.getRole().name());

        return new LoginResponseDto(token, customer.getId(), customer.getUsername(), customer.getRole().name());
    }


    @Transactional
    public CustomerResponseDto adminUpdateCustomer(Long id, CustomerRequestDto dto, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        Customer targetCustomer = customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Target customer not found or deleted"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(targetCustomer.getId())) {
            throw new AuthorizationDeniedException("You are not authorized to update this user");
        }

        targetCustomer.setName(dto.name());
        targetCustomer.setSurname(dto.surname());
        targetCustomer.setEmail(dto.email());
        targetCustomer.setUsername(dto.username());
        targetCustomer.setPassword(passwordEncoder.encode(dto.password()));

        Customer updated = customerRepository.save(targetCustomer);
        return CustomerMapper.toDto(updated);
    }

    @Transactional
    public CustomerResponseDto updateCurrentCustomer(CustomerRequestDto dto, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        currentUser.setName(dto.name());
        currentUser.setSurname(dto.surname());
        currentUser.setEmail(dto.email());
        currentUser.setUsername(dto.username());
        currentUser.setPassword(passwordEncoder.encode(dto.password()));

        Customer updated = customerRepository.save(currentUser);
        return CustomerMapper.toDto(updated);

    }

    public CustomerResponseDto getCustomerById(Long id, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        Customer targetCustomer = customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found or deleted"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(targetCustomer.getId())) {
            throw new AuthorizationDeniedException("You are not authorized to view this user");
        }

        return CustomerMapper.toDto(targetCustomer);
    }

    public CustomerResponseDto getCurrentCustomer(String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        return CustomerMapper.toDto(currentUser);
    }


}
