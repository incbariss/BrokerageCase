package task.ing.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import task.ing.exceptions.*;
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

    public static final String TRY = "TRY";


    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto, String currentUsername) {
        return switch (dto.orderSide()) {
            case BUY -> createBuyOrder(dto, currentUsername);
            case SELL -> createSellOrder(dto, currentUsername);
            default -> throw new IllegalArgumentException("Invalid order side");
        };
    }

    private OrderResponseDto createBuyOrder(OrderRequestDto dto, String currentUsername) {
        Customer customer = validateCustomer(currentUsername);
        AssetList assetList = getAssetList(dto.assetName());

        BigDecimal totalCost = dto.price().multiply(BigDecimal.valueOf(dto.size()));
        Asset tryAsset = getCustomerAsset(customer.getId(), TRY);

        if (tryAsset.getUsableSize() < totalCost.doubleValue()) {
            throw new InsufficientBalanceException("Insufficient TRY balance");
        }

        if (dto.price().compareTo(assetList.getCurrentPrice()) == 0) {
            tryAsset.setSize(tryAsset.getSize() - totalCost.doubleValue());
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost.doubleValue());
            assetRepository.save(tryAsset);

            Order order = saveMatchedOrder(dto, assetList, customer);
            updateAssetAfterBuy(customer, dto, assetList);

            return OrderMapper.toDto(order);
        } else {
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost.doubleValue());
            assetRepository.save(tryAsset);

            Order order = savePendingOrder(dto, assetList, customer);

            return OrderMapper.toDto(order);
        }
    }

    private OrderResponseDto createSellOrder(OrderRequestDto dto, String currentUsername) {
        Customer customer = validateCustomer(currentUsername);
        AssetList assetList = getAssetList(dto.assetName());

        Asset asset = getCustomerAsset(customer.getId(), dto.assetName());
        if (asset.getUsableSize() < dto.size()) {
            throw new InsufficientBalanceException("Insufficient asset balance");
        }

        BigDecimal totalCost = dto.price().multiply(BigDecimal.valueOf(dto.size()));

        if (dto.price().compareTo(assetList.getCurrentPrice()) == 0) {
            asset.setSize(asset.getSize() - dto.size());
            asset.setUsableSize(asset.getUsableSize() - dto.size());
            assetRepository.save(asset);

            Order order = saveMatchedOrder(dto, assetList, customer);
            updateAssetAfterSell(customer, totalCost);

            return OrderMapper.toDto(order);
        } else {
            asset.setUsableSize(asset.getUsableSize() - dto.size());
            assetRepository.save(asset);

            Order order = savePendingOrder(dto, assetList, customer);

            return OrderMapper.toDto(order);
        }
    }

    private Customer validateCustomer(String currentUsername) {
        return customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
    }

    private AssetList getAssetList(String assetName) {
        return assetListRepository.findByAssetName(assetName)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found"));
    }

    private Order saveMatchedOrder(OrderRequestDto dto, AssetList assetList, Customer customer) {
        Order order = OrderMapper.toEntity(dto, assetList, customer);
        order.setOrderStatus(OrderStatus.MATCHED);
        assetList.setCurrentPrice(dto.price());
        assetListRepository.save(assetList);
        return orderRepository.save(order);
    }

    private Order savePendingOrder(OrderRequestDto dto, AssetList assetList, Customer customer) {
        Order order = OrderMapper.toEntity(dto, assetList, customer);
        order.setOrderStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    private void updateAssetAfterBuy(Customer customer, OrderRequestDto dto, AssetList assetList) {
        Asset buyerAsset = getOrCreateCustomerAsset(customer, dto.assetName(), assetList);
        buyerAsset.setSize(buyerAsset.getSize() + dto.size());
        buyerAsset.setUsableSize(buyerAsset.getUsableSize() + dto.size());
        assetRepository.save(buyerAsset);
    }

    private void updateAssetAfterSell(Customer customer, BigDecimal totalCost) {
        Asset tryAsset = getCustomerAsset(customer.getId(), TRY);
        tryAsset.setSize(tryAsset.getSize() + totalCost.doubleValue());
        tryAsset.setUsableSize(tryAsset.getUsableSize() + totalCost.doubleValue());
        assetRepository.save(tryAsset);
    }


    @Transactional
    public void cancelOrder(Long orderId, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) &&
                !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new AuthorizationDeniedException("You are not authorized to cancel this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = getCustomerAsset(order.getCustomer().getId(), TRY);
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
    public List<OrderResponseDto> listOrdersForAdmin(Long customerId, LocalDate start, LocalDate end) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found or deleted"));

        List<Order> orders = orderRepository.findByCustomerIdAndCreatedDateBetween(customer.getId(), start, end);
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }


    @Transactional
    public List<OrderResponseDto> listOrdersForCurrentUser(LocalDate start, LocalDate end, String currentUsername) {
        Customer currentUser = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomerNotFoundException("Current user not found"));

        List<Order> orders = orderRepository.findByCustomerIdAndCreatedDateBetween(currentUser.getId(), start, end);
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderResponseDto depositForCurrentUser(double amount, String currentUsername) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Customer customer = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomerNotFoundException("Current user not found"));

        return depositInternal(customer, amount);
    }

    @Transactional
    public OrderResponseDto withdrawForCurrentUser(double amount, String currentUsername) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Customer customer = customerRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new CustomerNotFoundException("Current user not found"));

        return withdrawInternal(customer, amount);
    }
    private OrderResponseDto depositInternal(Customer customer, double amount) {
        AssetList tryAssetList = assetListRepository.findByAssetName(TRY)
                .orElseThrow(() -> new AssetNotFoundException("TRY asset not found"));

        Asset tryAsset = getCustomerAsset(customer.getId(), TRY);
        tryAsset.setSize(tryAsset.getSize() + amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + amount);
        assetRepository.save(tryAsset);

        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(TRY);
        order.setSize(amount);
        order.setPrice(BigDecimal.ONE);
        order.setOrderSide(OrderSide.BUY);
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setAssetList(tryAssetList);

        return OrderMapper.toDto(orderRepository.save(order));
    }

    private OrderResponseDto withdrawInternal(Customer customer, double amount) {
        AssetList tryAssetList = assetListRepository.findByAssetName(TRY)
                .orElseThrow(() -> new AssetNotFoundException("TRY asset not found"));

        Asset tryAsset = getCustomerAsset(customer.getId(), TRY);

        if (tryAsset.getUsableSize() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        tryAsset.setSize(tryAsset.getSize() - amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() - amount);
        assetRepository.save(tryAsset);

        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(TRY);
        order.setSize(amount);
        order.setPrice(BigDecimal.ONE);
        order.setOrderSide(OrderSide.SELL);
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setAssetList(tryAssetList);

        return OrderMapper.toDto(orderRepository.save(order));
    }




    @Transactional
    public void approveMatchedOrders(Long buyOrderId, Long sellOrderId) {
        Order buyOrder = orderRepository.findById(buyOrderId)
                .orElseThrow(() -> new OrderNotFoundException("Buy order not found"));

        Order sellOrder = orderRepository.findById(sellOrderId)
                .orElseThrow(() -> new OrderNotFoundException("Sell order not found"));

        if (buyOrder.getOrderStatus() != OrderStatus.PENDING || sellOrder.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Only PENDING orders can be matched");
        }

        if (buyOrder.getOrderSide() != OrderSide.BUY || sellOrder.getOrderSide() != OrderSide.SELL) {
            throw new InvalidOrderSideException("Order sides are not valid for matching");
        }

        if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) != 0) {
            throw new PriceMismatchException("Prices do not match");
        }

        if (!buyOrder.getAssetName().equals(sellOrder.getAssetName())) {
            throw new AssetMismatchException("Assets do not match");
        }

        BigDecimal matchedSize = BigDecimal.valueOf(buyOrder.getSize()).min(BigDecimal.valueOf(sellOrder.getSize()));
        BigDecimal totalPrice = sellOrder.getPrice().multiply(matchedSize);

        AssetList assetList = buyOrder.getAssetList();
        assetList.setCurrentPrice(buyOrder.getPrice());
        assetListRepository.save(assetList);

        Asset buyerTryAsset = getCustomerAsset(buyOrder.getCustomer().getId(), TRY);
        buyerTryAsset.setSize(BigDecimal.valueOf(buyerTryAsset.getSize()).subtract(totalPrice).doubleValue());
        assetRepository.save(buyerTryAsset);

        Asset buyerAsset = getOrCreateCustomerAsset(buyOrder.getCustomer(), buyOrder.getAssetName(), assetList);
        buyerAsset.setSize(BigDecimal.valueOf(buyerAsset.getSize()).add(matchedSize).doubleValue());
        buyerAsset.setUsableSize(BigDecimal.valueOf(buyerAsset.getUsableSize()).add(matchedSize).doubleValue());
        assetRepository.save(buyerAsset);

        Asset sellerTryAsset = getCustomerAsset(sellOrder.getCustomer().getId(), TRY);
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
                .orElseThrow(() -> new AssetNotFoundException("Asset not found for customer"));
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









