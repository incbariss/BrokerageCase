package task.ing.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task.ing.mapper.OrderMapper;
import task.ing.model.dto.request.OrderRequestDto;
import task.ing.model.dto.response.OrderResponseDto;
import task.ing.model.entity.Asset;
import task.ing.model.entity.AssetList;
import task.ing.model.entity.Customer;
import task.ing.model.entity.Order;
import task.ing.model.enums.OrderSide;
import task.ing.model.enums.OrderStatus;
import task.ing.repository.AssetListRepository;
import task.ing.repository.AssetRepository;
import task.ing.repository.CustomerRepository;
import task.ing.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final AssetListRepository assetListRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto) {
        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        AssetList assetList = assetListRepository.findByAssetName(dto.assetName())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        double totalCost = dto.price().doubleValue() * dto.size();

        if (dto.orderSide() == OrderSide.BUY) {
            Asset tryAsset = getCustomerAsset(customer.getId(), "TRY");
            if (tryAsset.getUsableSize() < totalCost) {
                throw new RuntimeException("Insufficient TRY balance");
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost);
            assetRepository.save(tryAsset);
        }

        else if (dto.orderSide() == OrderSide.SELL) {
            Asset asset = getCustomerAsset(customer.getId(), dto.assetName());
            if (asset.getUsableSize() < dto.size()) {
                throw new RuntimeException("Insufficient asset balance");
            }
            asset.setUsableSize(asset.getUsableSize() - dto.size());
            assetRepository.save(asset);
        }

        Order order = OrderMapper.toEntity(dto, assetList, customer);
        order = orderRepository.save(order);
        return OrderMapper.toDto(order);

    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be deleted");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = getCustomerAsset(order.getCustomer().getId(), "TRY");
            double refund = order.getPrice().doubleValue() * order.getSize();
            tryAsset.setUsableSize(tryAsset.getUsableSize() + refund);
            assetRepository.save(tryAsset);
        }

        else if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = getCustomerAsset(order.getCustomer().getId(), order.getAssetName());
            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetRepository.save(asset);
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }


    public List<OrderResponseDto> listOrders (Long customerId, LocalDate start, LocalDate end) {
        List<Order> orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, start, end);
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderResponseDto depositTRY(Long customerId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        AssetList tryAssetList = assetListRepository.findByAssetName("TRY")
                .orElseThrow(() -> new RuntimeException("TRY asset not found"));

        Asset tryAsset = getCustomerAsset(customerId, "TRY");
        tryAsset.setSize(tryAsset.getSize() + amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + amount);
        assetRepository.save(tryAsset);

        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName("TRY");
        order.setSize(amount);
        order.setPrice(BigDecimal.ONE);
//        order.setCreateDate(LocalDateTime.now());
        order.setOrderSide(OrderSide.BUY);
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setAssetList(tryAssetList);

        order = orderRepository.save(order);
        return OrderMapper.toDto(order);
    }

    private Asset getCustomerAsset(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new RuntimeException("Asset not found for customer"));
    }
}









