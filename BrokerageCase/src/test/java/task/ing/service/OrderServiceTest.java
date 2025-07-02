package task.ing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authorization.AuthorizationDeniedException;
import task.ing.exceptions.*;
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
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AssetListRepository assetListRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private final Long customerId = 1L;
    private final String username = "testUser";
    private final String assetName = "ASELS";
    private final BigDecimal price = BigDecimal.valueOf(45.75);
    private final double size = 2.0;
    private final Long orderId = 1L;

    private Customer mockCustomer() {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setUsername(username);
        return customer;
    }

    private AssetList mockAssetList(BigDecimal currentPrice) {
        AssetList assetList = new AssetList();
        assetList.setAssetName(assetName);
        assetList.setCurrentPrice(currentPrice);
        return assetList;
    }

    public static Asset mockAsset(String name, double size, double usableSize) {
        Asset asset = new Asset();
        asset.getAssetList().setAssetName(name);
        asset.setSize(size);
        asset.setUsableSize(usableSize);
        return asset;
    }

    private Order mockOrder(OrderSide orderSide, OrderStatus status, Customer customer) {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setOrderSide(orderSide);
        order.setOrderStatus(status);
        order.setAssetName(assetName);
        order.setPrice(price);
        order.setSize(size);
        return order;
    }

    private OrderRequestDto buildRequest(OrderSide side, BigDecimal price) {
        return new OrderRequestDto(assetName, price, size, side);
    }

    @Test
    void testCreateBuyOrder_withMatchingPrice_shouldReturnMatchedOrder() {
        // Arrange
        OrderRequestDto dto = buildRequest(OrderSide.BUY, price);

        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset tryAsset = mockAsset("TRY", 1000.0, 1000.0);
        Asset buyerAsset = mockAsset(assetName, 0.0, 0.0);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(buyerAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto response = orderService.createOrder(dto, username);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.MATCHED, response.orderStatus());
        assertEquals(assetName, response.assetName());
        assertEquals(price, response.price());
        assertEquals(size, response.size());

        // Capture and verify saved order
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.MATCHED, savedOrder.getOrderStatus());
        assertEquals(assetName, savedOrder.getAssetName());
        assertEquals(price, savedOrder.getPrice());
        assertEquals(size, savedOrder.getSize());
        assertEquals(OrderSide.BUY, savedOrder.getOrderSide());

        // Ek olarak TRY ve ASELS varlıklarının güncellenip kaydedildiğini doğrula
        verify(assetRepository).save(tryAsset);
        verify(assetRepository).save(buyerAsset);
    }


    @Test
    void testCreateSellOrder_withMatchingPrice_shouldReturnMatchedOrder() {
        // Arrange
        OrderRequestDto dto = buildRequest(OrderSide.SELL, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset asset = mockAsset(assetName, 10.0, 10.0); // yeterli varlık
        Asset tryAsset = mockAsset("TRY", 100.0, 100.0); // TRY bakiyesi

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto response = orderService.createOrder(dto, username);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.MATCHED, response.orderStatus());
        assertEquals(assetName, response.assetName());
        assertEquals(price, response.price());
        assertEquals(size, response.size());

        // Capture and verify saved order
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.MATCHED, savedOrder.getOrderStatus());
        assertEquals(OrderSide.SELL, savedOrder.getOrderSide());

        verify(assetRepository).save(asset);
        verify(assetRepository).save(tryAsset);
    }


    @Test
    void testCreateBuyOrder_withNonMatchingPrice_shouldReturnPendingOrder() {
        // Arrange
        BigDecimal requestPrice = price.subtract(BigDecimal.valueOf(5)); // eşleşmeyen fiyat
        OrderRequestDto dto = buildRequest(OrderSide.BUY, requestPrice);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset tryAsset = mockAsset("TRY", 1000, 1000);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto response = orderService.createOrder(dto, username);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.orderStatus());
        assertEquals(assetName, response.assetName());
        assertEquals(requestPrice, response.price());
        assertEquals(size, response.size());

        // Capture and verify saved order
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.PENDING, savedOrder.getOrderStatus());
        assertEquals(OrderSide.BUY, savedOrder.getOrderSide());

        verify(assetRepository).save(tryAsset);
    }


    @Test
    void testCreateSellOrder_withNonMatchingPrice_shouldReturnPendingOrder() {
        // Arrange
        BigDecimal requestPrice = price.add(BigDecimal.valueOf(10)); // eşleşmeyen fiyat
        OrderRequestDto dto = buildRequest(OrderSide.SELL, requestPrice);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset asset = mockAsset(assetName, 10.0, 10.0); // yeterli varlık

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto response = orderService.createOrder(dto, username);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.orderStatus());
        assertEquals(assetName, response.assetName());
        assertEquals(requestPrice, response.price());
        assertEquals(size, response.size());

        // Capture and verify saved order
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.PENDING, savedOrder.getOrderStatus());
        assertEquals(OrderSide.SELL, savedOrder.getOrderSide());

        verify(assetRepository).save(asset);
    }


    @Test
    void testCreateBuyOrder_withInsufficientTRYBalance_shouldThrowException() {
        // Arrange
        OrderRequestDto dto = buildRequest(OrderSide.BUY, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset tryAsset = mockAsset("TRY", 50.0, 50.0); // Yetersiz usableSize (totalCost = 91.5)

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> orderService.createOrder(dto, username)
        );

        assertEquals("Insufficient TRY balance", exception.getMessage());

        // Verify
        verify(customerRepository).findByUsername(username);
        verify(assetListRepository).findByAssetName(assetName);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, "TRY");
    }


    @Test
    void testCreateSellOrder_withInsufficientAsset_shouldThrowException() {
        // Arrange
        OrderRequestDto dto = buildRequest(OrderSide.SELL, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset asset = mockAsset(assetName, 10.0, 1.0); // usableSize yetersiz (size = 2.0)

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> orderService.createOrder(dto, username)
        );

        assertEquals("Insufficient asset balance", exception.getMessage());

        // Verify
        verify(customerRepository).findByUsername(username);
        verify(assetListRepository).findByAssetName(assetName);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
    }


// CANCEL ORDER

    @Test
    void cancelOrder_shouldCancelBuyOrderAndRefundTRY() {
        // Arrange
        Customer customer = mockCustomer();
        customer.setRole(Role.ROLE_USER);

        Order order = mockOrder(OrderSide.BUY, OrderStatus.PENDING, customer);

        Asset tryAsset = mockAsset("TRY", 500.0, 400.0); // usableSize = 400

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.cancelOrder(orderId, username);

        // Assert
        double expectedRefund = price.doubleValue() * size;
        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
        assertEquals(400.0 + expectedRefund, tryAsset.getUsableSize());

        // Verify
        verify(customerRepository).findByUsername(username);
        verify(orderRepository).findById(orderId);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, "TRY");
        verify(assetRepository).save(tryAsset);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldCancelSellOrderAndRestoreAsset() {
        // Arrange
        Customer customer = mockCustomer();
        customer.setRole(Role.ROLE_USER);

        Order order = mockOrder(OrderSide.SELL, OrderStatus.PENDING, customer);


        Asset asset = mockAsset(assetName, 10.0, 8.0);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.cancelOrder(orderId, username);

        // Assert
        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
        assertEquals(8.0 + size, asset.getUsableSize());

        // Verify
        verify(customerRepository).findByUsername(username);
        verify(orderRepository).findById(orderId);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
        verify(assetRepository).save(asset);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderIsNotPending() {
        Customer customer = mockCustomer();
        customer.setRole(Role.ROLE_USER);

        Order order = mockOrder(OrderSide.BUY, OrderStatus.MATCHED, customer);


        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.cancelOrder(orderId, username)
        );

        assertEquals("Only pending orders can be cancelled", exception.getMessage());
    }

    @Test
    void cancelOrder_shouldThrowException_whenUserIsNotOwnerOrAdmin() {
        Customer currentUser = mockCustomer();
        currentUser.setId(2L); // farklı kullanıcı
        currentUser.setRole(Role.ROLE_USER);

        Customer orderOwner = new Customer();
        orderOwner.setId(1L);

        Order order = mockOrder(OrderSide.BUY, OrderStatus.PENDING, orderOwner);


        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        AuthorizationDeniedException exception = assertThrows(
                AuthorizationDeniedException.class,
                () -> orderService.cancelOrder(orderId, username)
        );

        assertEquals("You are not authorized to cancel this order", exception.getMessage());
    }

    @Test
    void cancelOrder_shouldAllowAdminToCancelOthersOrder() {
        Customer admin = mockCustomer();
        admin.setRole(Role.ROLE_ADMIN);

        Customer orderOwner = new Customer();
        orderOwner.setId(99L);

        Order order = mockOrder(OrderSide.SELL, OrderStatus.PENDING, orderOwner);


        Asset asset = mockAsset(assetName, 10.0, 5.0);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(admin));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(orderOwner.getId(), assetName)).thenReturn(Optional.of(asset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(orderId, username);

        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
        assertEquals(5.0 + size, asset.getUsableSize());

        verify(assetRepository).save(asset);
        verify(orderRepository).save(order);
    }


    //approve match orders

    @Test
    void approveMatchedOrders_shouldMatchOrdersAndUpdateAssets() {
        // Arrange
        Long buyOrderId = 1L;
        Long sellOrderId = 2L;

        Customer buyer = mockCustomer();
        buyer.setId(buyOrderId);
        Customer seller = new Customer();
        seller.setId(sellOrderId);

        AssetList assetList = mockAssetList(price);

        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.PENDING, buyer);
        buyOrder.setId(buyOrderId);
        buyOrder.setAssetList(assetList);

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(sellOrderId);
        sellOrder.setAssetList(assetList);

        Asset buyerTry = mockAsset("TRY", 1000.0, 1000.0);
        Asset buyerAsset = mockAsset(assetName, 0.0, 0.0);
        Asset sellerTry = mockAsset("TRY", 100.0, 100.0);
        Asset sellerAsset = mockAsset(assetName, 5.0, 5.0);

        when(orderRepository.findById(buyOrderId)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(sellOrderId)).thenReturn(Optional.of(sellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "TRY")).thenReturn(Optional.of(buyerTry));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), assetName)).thenReturn(Optional.of(buyerAsset));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "TRY")).thenReturn(Optional.of(sellerTry));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), assetName)).thenReturn(Optional.of(sellerAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.approveMatchedOrders(buyOrderId, sellOrderId);

        // Assert
        assertEquals(OrderStatus.MATCHED, buyOrder.getOrderStatus());
        assertEquals(OrderStatus.MATCHED, sellOrder.getOrderStatus());

        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(size));
        assertEquals(1000.0 - totalPrice.doubleValue(), buyerTry.getSize());
        assertEquals(0.0 + size, buyerAsset.getSize());
        assertEquals(100.0 + totalPrice.doubleValue(), sellerTry.getSize());
        assertEquals(5.0 - size, sellerAsset.getSize());

        verify(assetRepository).save(buyerTry);
        verify(assetRepository).save(buyerAsset);
        verify(assetRepository).save(sellerTry);
        verify(assetRepository).save(sellerAsset);
        verify(orderRepository).save(buyOrder);
        verify(orderRepository).save(sellOrder);
        verify(assetListRepository).save(assetList);
    }


    @Test
    void approveMatchedOrders_shouldThrowException_whenPricesDoNotMatch() {
        // Arrange
        Customer buyer = mockCustomer();
        Customer seller = new Customer();
        seller.setId(2L);

        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.PENDING, buyer);
        buyOrder.setId(1L);
        buyOrder.setPrice(BigDecimal.valueOf(50));

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(2L);
        sellOrder.setPrice(BigDecimal.valueOf(55)); // uyuşmayan fiyat

        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(sellOrder));

        // Act & Assert
        assertThrows(PriceMismatchException.class,
                () -> orderService.approveMatchedOrders(1L, 2L));
    }


    @Test
    void approveMatchedOrders_shouldThrowException_whenOrderStatusIsNotPending() {
        // Arrange
        Customer buyer = mockCustomer();
        Customer seller = new Customer();
        seller.setId(2L);

        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.MATCHED, buyer); // geçersiz durum
        buyOrder.setId(1L);

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(2L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(sellOrder));

        // Act & Assert
        assertThrows(InvalidOrderStatusException.class,
                () -> orderService.approveMatchedOrders(1L, 2L));
    }


    @Test
    void approveMatchedOrders_shouldThrowException_whenOrderSidesAreInvalid() {
        // Arrange
        Customer buyer = mockCustomer();
        Customer seller = new Customer();
        seller.setId(2L);

        Order buyOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, buyer); // yanlış yön
        buyOrder.setId(1L);

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(2L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(sellOrder));

        // Act & Assert
        assertThrows(InvalidOrderSideException.class,
                () -> orderService.approveMatchedOrders(1L, 2L));
    }


    @Test
    void approveMatchedOrders_shouldThrowException_whenAssetsDoNotMatch() {
        // Arrange
        Customer buyer = mockCustomer();
        Customer seller = new Customer();
        seller.setId(2L);

        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.PENDING, buyer);
        buyOrder.setId(1L);
        buyOrder.setAssetName("ASELS");

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(2L);
        sellOrder.setAssetName("THYAO"); // farklı varlık

        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(sellOrder));

        // Act & Assert
        assertThrows(AssetMismatchException.class,
                () -> orderService.approveMatchedOrders(1L, 2L));
    }


    @Test
    void approveMatchedOrders_shouldThrowException_whenBuyOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.approveMatchedOrders(1L, 2L));
    }

    @Test
    void approveMatchedOrders_shouldThrowException_whenSellOrderNotFound() {
        // Arrange
        Customer buyer = mockCustomer();
        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.PENDING, buyer);
        buyOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class,
                () -> orderService.approveMatchedOrders(1L, 2L));
    }


    @Test
    void approveMatchedOrders_shouldSetBuyOrderToPending_whenPartiallyMatched() {
        // Arrange
        Long buyOrderId = 1L;
        Long sellOrderId = 2L;

        Customer buyer = mockCustomer();
        buyer.setId(buyOrderId);
        Customer seller = new Customer();
        seller.setId(sellOrderId);

        AssetList assetList = mockAssetList(price);

        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.PENDING, buyer);
        buyOrder.setId(buyOrderId);
        buyOrder.setSize(5.0);
        buyOrder.setAssetList(assetList);

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(sellOrderId);
        sellOrder.setSize(2.0);
        sellOrder.setAssetList(assetList);

        Asset buyerTry = mockAsset("TRY", 1000.0, 1000.0);
        Asset buyerAsset = mockAsset(assetName, 0.0, 0.0);
        Asset sellerTry = mockAsset("TRY", 100.0, 100.0);
        Asset sellerAsset = mockAsset(assetName, 5.0, 5.0);

        when(orderRepository.findById(buyOrderId)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(sellOrderId)).thenReturn(Optional.of(sellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "TRY")).thenReturn(Optional.of(buyerTry));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), assetName)).thenReturn(Optional.of(buyerAsset));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "TRY")).thenReturn(Optional.of(sellerTry));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), assetName)).thenReturn(Optional.of(sellerAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.approveMatchedOrders(buyOrderId, sellOrderId);

        // Assert
        assertEquals(OrderStatus.PENDING, buyOrder.getOrderStatus());
        assertEquals(3.0, buyOrder.getSize()); // 5 - 2

        assertEquals(OrderStatus.MATCHED, sellOrder.getOrderStatus());
        assertEquals(2.0, sellOrder.getSize());
    }


    @Test
    void approveMatchedOrders_shouldSetSellOrderToPending_whenPartiallyMatched() {
        // Arrange
        Long buyOrderId = 1L;
        Long sellOrderId = 2L;

        Customer buyer = mockCustomer();
        buyer.setId(buyOrderId);
        Customer seller = new Customer();
        seller.setId(sellOrderId);

        AssetList assetList = mockAssetList(price);

        Order buyOrder = mockOrder(OrderSide.BUY, OrderStatus.PENDING, buyer);
        buyOrder.setId(buyOrderId);
        buyOrder.setSize(2.0);
        buyOrder.setAssetList(assetList);

        Order sellOrder = mockOrder(OrderSide.SELL, OrderStatus.PENDING, seller);
        sellOrder.setId(sellOrderId);
        sellOrder.setSize(5.0);
        sellOrder.setAssetList(assetList);

        Asset buyerTry = mockAsset("TRY", 1000.0, 1000.0);
        Asset buyerAsset = mockAsset(assetName, 0.0, 0.0);
        Asset sellerTry = mockAsset("TRY", 100.0, 100.0);
        Asset sellerAsset = mockAsset(assetName, 10.0, 10.0);

        when(orderRepository.findById(buyOrderId)).thenReturn(Optional.of(buyOrder));
        when(orderRepository.findById(sellOrderId)).thenReturn(Optional.of(sellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), "TRY")).thenReturn(Optional.of(buyerTry));
        when(assetRepository.findByCustomerIdAndAssetName(buyer.getId(), assetName)).thenReturn(Optional.of(buyerAsset));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), "TRY")).thenReturn(Optional.of(sellerTry));
        when(assetRepository.findByCustomerIdAndAssetName(seller.getId(), assetName)).thenReturn(Optional.of(sellerAsset));
        when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.approveMatchedOrders(buyOrderId, sellOrderId);

        // Assert
        assertEquals(OrderStatus.MATCHED, buyOrder.getOrderStatus());
        assertEquals(2.0, buyOrder.getSize());

        assertEquals(OrderStatus.PENDING, sellOrder.getOrderStatus());
        assertEquals(3.0, sellOrder.getSize()); // 5 - 2
    }


    //LIST ORDER

    @Test
    void listOrdersForAdmin_shouldReturnOrders_whenCustomerExistsAndOrdersFound() {
        Customer customer = mockCustomer();
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        Order order1 = mockOrder(OrderSide.BUY, OrderStatus.MATCHED, customer);
        Order order2 = mockOrder(OrderSide.SELL, OrderStatus.PENDING, customer);
        List<Order> orders = List.of(order1, order2);

        when(customerRepository.findByIdAndIsDeletedFalse(customer.getId())).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerIdAndCreatedDateBetween(customer.getId(), start, end)).thenReturn(orders);

        List<OrderResponseDto> result = orderService.listOrdersForAdmin(customer.getId(), start, end);

        assertEquals(2, result.size());
        verify(customerRepository).findByIdAndIsDeletedFalse(customer.getId());
        verify(orderRepository).findByCustomerIdAndCreatedDateBetween(customer.getId(), start, end);
    }


    @Test
    void listOrdersForAdmin_shouldThrowException_whenCustomerNotFound() {
        Long customerId = 99L;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> orderService.listOrdersForAdmin(customerId, start, end));

        verify(customerRepository).findByIdAndIsDeletedFalse(customerId);
        verify(orderRepository, never())
                .findByCustomerIdAndCreatedDateBetween(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.<LocalDate>any(),
                        ArgumentMatchers.<LocalDate>any()
                );

    }


    @Test
    void listOrdersForCurrentUser_shouldReturnOrders_whenUserExistsAndOrdersFound() {
        String username = "testUser";
        Customer customer = mockCustomer();
        customer.setUsername(username);

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        Order order1 = mockOrder(OrderSide.BUY, OrderStatus.MATCHED, customer);
        Order order2 = mockOrder(OrderSide.SELL, OrderStatus.PENDING, customer);
        List<Order> orders = List.of(order1, order2);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerIdAndCreatedDateBetween(customer.getId(), start, end)).thenReturn(orders);

        List<OrderResponseDto> result = orderService.listOrdersForCurrentUser(start, end, username);

        assertEquals(2, result.size());
        verify(customerRepository).findByUsername(username);
        verify(orderRepository).findByCustomerIdAndCreatedDateBetween(customer.getId(), start, end);
    }

    @Test
    void listOrdersForCurrentUser_shouldThrowException_whenUserNotFound() {
        String username = "nonexistentUser";
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> orderService.listOrdersForCurrentUser(start, end, username));

        verify(customerRepository).findByUsername(username);
        verify(orderRepository, never())
                .findByCustomerIdAndCreatedDateBetween(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.<LocalDate>any(),
                        ArgumentMatchers.<LocalDate>any()
                );
    }


    //DEPOSIT & WITHDRAW

    @Test
    void depositForCurrentUser_shouldDepositSuccessfully_whenAmountIsValid() {
        String username = "testUser";
        double amount = 100.0;
        Customer customer = mockCustomer();
        customer.setUsername(username);

        Asset tryAsset = mockAsset("TRY", 500.0, 500.0);
        AssetList tryAssetList = mockAssetList(BigDecimal.ONE);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName("TRY")).thenReturn(Optional.of(tryAssetList));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(Mockito.any(Order.class)))
                .thenAnswer(invocation -> invocation.<Order>getArgument(0));


        OrderResponseDto result = orderService.depositForCurrentUser(amount, username);

        assertEquals("TRY", result.assetName());
        assertEquals(amount, result.size());
        assertEquals(OrderSide.BUY, result.orderSide());
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void depositForCurrentUser_shouldThrowException_whenAmountIsZeroOrNegative() {
        String username = "testUser";

        assertThrows(IllegalArgumentException.class,
                () -> orderService.depositForCurrentUser(0, username));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.depositForCurrentUser(-50, username));
    }

    @Test
    void depositForCurrentUser_shouldThrowException_whenUserNotFound() {
        String username = "unknownUser";

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> orderService.depositForCurrentUser(100, username));
    }

    @Test
    void withdrawForCurrentUser_shouldWithdrawSuccessfully_whenBalanceIsSufficient() {
        String username = "testUser";
        double amount = 100.0;
        Customer customer = mockCustomer();
        customer.setUsername(username);

        Asset tryAsset = mockAsset("TRY", 500.0, 500.0);
        AssetList tryAssetList = mockAssetList(BigDecimal.ONE);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName("TRY")).thenReturn(Optional.of(tryAssetList));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(Mockito.any(Order.class)))
                .thenAnswer(invocation -> invocation.<Order>getArgument(0));


        OrderResponseDto result = orderService.withdrawForCurrentUser(amount, username);

        assertEquals("TRY", result.assetName());
        assertEquals(amount, result.size());
        assertEquals(OrderSide.SELL, result.orderSide());
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void withdrawForCurrentUser_shouldThrowException_whenBalanceIsInsufficient() {
        String username = "testUser";
        double amount = 1000.0;
        Customer customer = mockCustomer();
        customer.setUsername(username);

        Asset tryAsset = mockAsset("TRY", 500.0, 500.0);
        AssetList tryAssetList = mockAssetList(BigDecimal.ONE);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName("TRY")).thenReturn(Optional.of(tryAssetList));
        when(assetRepository.findByCustomerIdAndAssetName(customer.getId(), "TRY")).thenReturn(Optional.of(tryAsset));

        assertThrows(InsufficientBalanceException.class,
                () -> orderService.withdrawForCurrentUser(amount, username));
    }

    @Test
    void withdrawForCurrentUser_shouldThrowException_whenAmountIsZeroOrNegative() {
        String username = "testUser";

        assertThrows(IllegalArgumentException.class,
                () -> orderService.withdrawForCurrentUser(0, username));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.withdrawForCurrentUser(-100, username));
    }

    @Test
    void withdrawForCurrentUser_shouldThrowException_whenUserNotFound() {
        String username = "unknownUser";

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> orderService.withdrawForCurrentUser(100, username));
    }


}





































