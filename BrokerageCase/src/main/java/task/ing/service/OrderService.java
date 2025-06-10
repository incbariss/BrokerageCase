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
import task.ing.model.enums.Role;
import task.ing.repository.AssetListRepository;
import task.ing.repository.AssetRepository;
import task.ing.repository.CustomerRepository;
import task.ing.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final AssetListRepository assetListRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!currentUser.getId().equals(dto.customerId())) {
            throw new RuntimeException("You are not authorized to create an order for another user");
        }

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(dto.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found or deleted"));

        AssetList assetList = assetListRepository.findByAssetName(dto.assetName())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        BigDecimal totalCost = dto.price().multiply(BigDecimal.valueOf(dto.size()));

        if (dto.orderSide() == OrderSide.BUY) {
            Asset tryAsset = getCustomerAsset(customer.getId(), "TRY");
            if (tryAsset.getUsableSize() < totalCost.doubleValue()) {
                throw new RuntimeException("Insufficient TRY balance");
            }

            if (dto.price().compareTo(assetList.getCurrentPrice()) == 0) {
                tryAsset.setSize(tryAsset.getSize() - totalCost.doubleValue());
                tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost.doubleValue());
                assetRepository.save(tryAsset);

                Order order = OrderMapper.toEntity(dto, assetList, customer);
                order.setOrderStatus(OrderStatus.MATCHED);
                order = orderRepository.save(order);

                assetList.setCurrentPrice(dto.price());
                assetListRepository.save(assetList);

                Asset buyerAsset = getOrCreateCustomerAsset(customer, dto.assetName(), assetList);
                buyerAsset.setSize(buyerAsset.getSize() + dto.size());
                buyerAsset.setUsableSize(buyerAsset.getUsableSize() + dto.size());
                assetRepository.save(buyerAsset);

                return OrderMapper.toDto(order);
            } else {
                tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost.doubleValue());

                Order order = OrderMapper.toEntity(dto, assetList, customer);
                order.setOrderStatus(OrderStatus.PENDING);
                return OrderMapper.toDto(orderRepository.save(order));
            }
        } else if (dto.orderSide() == OrderSide.SELL) {
            Asset asset = getCustomerAsset(customer.getId(), dto.assetName());
            if (asset.getUsableSize() < dto.size()) {
                throw new RuntimeException("Insufficient asset balance");
            }

            if (dto.price().compareTo(assetList.getCurrentPrice()) == 0) {
                asset.setSize(asset.getSize() - dto.size());
                asset.setUsableSize(asset.getUsableSize() - dto.size());
                assetRepository.save(asset);

                Order order = OrderMapper.toEntity(dto, assetList, customer);
                order.setOrderStatus(OrderStatus.MATCHED);
                order = orderRepository.save(order);

                assetList.setCurrentPrice(dto.price());
                assetListRepository.save(assetList);

                Asset sellerTryAsset = getCustomerAsset(customer.getId(), "TRY");
                sellerTryAsset.setSize(sellerTryAsset.getSize() + totalCost.doubleValue());
                sellerTryAsset.setUsableSize(sellerTryAsset.getUsableSize() + totalCost.doubleValue());
                assetRepository.save(sellerTryAsset);

                return OrderMapper.toDto(order);
            } else {
                asset.setUsableSize(asset.getUsableSize() - dto.size());

                Order order = OrderMapper.toEntity(dto, assetList, customer);
                order.setOrderStatus(OrderStatus.PENDING);
                return OrderMapper.toDto(orderRepository.save(order));
            }
        }

        throw new RuntimeException("Invalid order side");
    }


    @Transactional
    public void cancelOrder(Long orderId, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) &&
                !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be deleted");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = getCustomerAsset(order.getCustomer().getId(), "TRY");
            double refund = order.getPrice().doubleValue() * order.getSize();
            tryAsset.setUsableSize(tryAsset.getUsableSize() + refund);
            assetRepository.save(tryAsset);
        } else if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = getCustomerAsset(order.getCustomer().getId(), order.getAssetName());
            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetRepository.save(asset);
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }


    @Transactional
    public List<OrderResponseDto> listOrders(Long customerId, LocalDate start, LocalDate end, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(customerId)) {
            throw new RuntimeException("You are not authorized to view these orders");
        }

        List<Order> orders = orderRepository.findByCustomerIdAndCreatedDateBetween(customerId, start, end);
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderResponseDto depositTRY(Long customerId, double amount, String currentUsername) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(customerId)) {
            throw new RuntimeException("You are not authorized to deposit for another user");
        }

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found or deleted"));

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
        order.setOrderSide(OrderSide.BUY);
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setAssetList(tryAssetList);

        order = orderRepository.save(order);
        return OrderMapper.toDto(order);
    }

    @Transactional
    public OrderResponseDto withdrawTRY(Long customerId, double amount, String currentUsername) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(customerId)) {
            throw new RuntimeException("You are not authorized to withdraw for another user");
        }

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found or deleted"));

        AssetList tryAssetList = assetListRepository.findByAssetName("TRY")
                .orElseThrow(() -> new RuntimeException("TRY asset not found"));

        Asset tryAsset = getCustomerAsset(customerId, "TRY");

        if (tryAsset.getUsableSize() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        tryAsset.setSize(tryAsset.getSize() - amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() - amount);
        assetRepository.save(tryAsset);

        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName("TRY");
        order.setSize(amount);
        order.setPrice(BigDecimal.ONE);
        order.setOrderSide(OrderSide.SELL);
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setAssetList(tryAssetList);

        order = orderRepository.save(order);
        return OrderMapper.toDto(order);
    }


    @Transactional
    public void approveMatchedOrders(Long buyOrderId, Long sellOrderId) {
        Order buyOrder = orderRepository.findById(buyOrderId)
                .orElseThrow(() -> new RuntimeException("Buy order not found"));

        Order sellOrder = orderRepository.findById(sellOrderId)
                .orElseThrow(() -> new RuntimeException("Sell order not found"));

        if (buyOrder.getOrderStatus() != OrderStatus.PENDING || sellOrder.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be matched");
        }

        if (buyOrder.getOrderSide() != OrderSide.BUY || sellOrder.getOrderSide() != OrderSide.SELL) {
            throw new RuntimeException("Order sides are not valid for matching");
        }

        if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) != 0) {
            throw new RuntimeException("Prices do not match");
        }

        if (!buyOrder.getAssetName().equals(sellOrder.getAssetName())) {
            throw new RuntimeException("Assets do not match");
        }

        BigDecimal matchedSize = BigDecimal.valueOf(buyOrder.getSize()).min(BigDecimal.valueOf(sellOrder.getSize()));
        BigDecimal totalPrice = sellOrder.getPrice().multiply(matchedSize);

        AssetList assetList = buyOrder.getAssetList();
        assetList.setCurrentPrice(buyOrder.getPrice());
        assetListRepository.save(assetList);

        Asset buyerTryAsset = getCustomerAsset(buyOrder.getCustomer().getId(), "TRY");
        buyerTryAsset.setSize(BigDecimal.valueOf(buyerTryAsset.getSize()).subtract(totalPrice).doubleValue());
        assetRepository.save(buyerTryAsset);

        Asset buyerAsset = getOrCreateCustomerAsset(buyOrder.getCustomer(), buyOrder.getAssetName(), assetList);
        buyerAsset.setSize(BigDecimal.valueOf(buyerAsset.getSize()).add(matchedSize).doubleValue());
        buyerAsset.setUsableSize(BigDecimal.valueOf(buyerAsset.getUsableSize()).add(matchedSize).doubleValue());
        assetRepository.save(buyerAsset);

        Asset sellerTryAsset = getCustomerAsset(sellOrder.getCustomer().getId(), "TRY");
        sellerTryAsset.setSize(BigDecimal.valueOf(sellerTryAsset.getSize()).add(totalPrice).doubleValue());
        sellerTryAsset.setUsableSize(BigDecimal.valueOf(sellerTryAsset.getUsableSize()).add(totalPrice).doubleValue());
        assetRepository.save(sellerTryAsset);

        Asset sellerAsset = getCustomerAsset(sellOrder.getCustomer().getId(), sellOrder.getAssetName());
        sellerAsset.setSize(BigDecimal.valueOf(sellerAsset.getSize()).subtract(matchedSize).doubleValue());
        assetRepository.save(sellerAsset);

        BigDecimal buyRemaining = BigDecimal.valueOf(buyOrder.getSize()).subtract(matchedSize);
        BigDecimal sellRemaining = BigDecimal.valueOf(sellOrder.getSize()).subtract(matchedSize);

        if (buyRemaining.compareTo(BigDecimal.ZERO) > 0) {
            buyOrder.setSize(buyRemaining.doubleValue());
            buyOrder.setOrderStatus(OrderStatus.PENDING);
        } else {
            buyOrder.setOrderStatus(OrderStatus.MATCHED);
        }

        if (sellRemaining.compareTo(BigDecimal.ZERO) > 0) {
            sellOrder.setSize(sellRemaining.doubleValue());
            sellOrder.setOrderStatus(OrderStatus.PENDING);
        } else {
            sellOrder.setOrderStatus(OrderStatus.MATCHED);
        }

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);


    }

    private Asset getCustomerAsset(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new RuntimeException("Asset not found for customer"));
    }

    private Asset getOrCreateCustomerAsset(Customer customer, String assetName, AssetList assetList) {
        return assetRepository.findByCustomerIdAndAssetName(customer.getId(), assetName)
                .orElseGet(() -> {
                    Asset asset = new Asset();
                    asset.setCustomer(customer);
                    asset.setAssetName(assetName);
                    asset.setSize(0);
                    asset.setUsableSize(0);
                    asset.setAssetList(assetList);
                    return asset;
                });
    }

}









