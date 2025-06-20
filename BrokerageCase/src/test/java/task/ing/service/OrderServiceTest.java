package task.ing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AssetListRepository assetListRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderRepository orderRepository;

    private final Long customerId = 1L;
    private final String username = "testUser";
    private final String assetName = "ASELS";
    private final BigDecimal price = BigDecimal.valueOf(45.75);
    private final double size = 2.0;
    private final BigDecimal totalCost = price.multiply(BigDecimal.valueOf(size));

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

    private Asset mockAsset(String name, double size, double usableSize) {
        Asset asset = new Asset();
        asset.setAssetName(name);
        asset.setSize(size);
        asset.setUsableSize(usableSize);
        return asset;
    }

    private OrderRequestDto buildRequest(OrderSide side, BigDecimal price) {
        return new OrderRequestDto(customerId, assetName, price, size, side);
    }

    @Test
    void testBuyOrderWithMatchingPrice() {
        // Arrange
        OrderRequestDto dto = buildRequest(OrderSide.BUY, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset tryAsset = mockAsset("TRY", 1000, 1000);
        Asset buyerAsset = mockAsset(assetName, 0, 0);
        Order order = new Order();
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setPrice(price);
        order.setSize(size);
        order.setOrderSide(OrderSide.BUY);
        order.setCreatedDate(LocalDate.now());

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(buyerAsset));
        when(orderRepository.save(ArgumentMatchers.<Order>any())).thenReturn(order);

        // Act
        OrderResponseDto response = orderService.createOrder(dto, username);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.MATCHED, response.orderStatus());
        assertEquals(assetName, response.assetName());
        assertEquals(customerId, response.customerId());
        assertEquals(price, response.price());
        assertEquals(size, response.size());

        // Verify repository interactions
        verify(customerRepository).findByUsername(username);
        verify(customerRepository).findByIdAndIsDeletedFalse(customerId);
        verify(assetListRepository).findByAssetName(assetName);
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, "TRY");
        verify(assetRepository).findByCustomerIdAndAssetName(customerId, assetName);
        verify(orderRepository).save(ArgumentMatchers.<Order>any());
    }


    @Test
    void testBuyOrderWithNonMatchingPrice() {
        OrderRequestDto dto = buildRequest(OrderSide.BUY, BigDecimal.valueOf(90));
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(BigDecimal.valueOf(100));
        Asset tryAsset = mockAsset("TRY", 1000, 1000);
        Order order = new Order();
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setPrice(dto.price());
        order.setSize(size);
        order.setOrderSide(OrderSide.BUY);
        order.setCreatedDate(LocalDate.now());

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(ArgumentMatchers.<Order>any())).thenReturn(order);

        OrderResponseDto response = orderService.createOrder(dto, username);

        assertEquals(OrderStatus.PENDING, response.orderStatus());
    }

    @Test
    void testSellOrderWithMatchingPrice() {
        OrderRequestDto dto = buildRequest(OrderSide.SELL, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset asset = mockAsset(assetName, 10, 10);
        Asset tryAsset = mockAsset("TRY", 0, 0);
        Order order = new Order();
        order.setOrderStatus(OrderStatus.MATCHED);
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setPrice(price);
        order.setSize(size);
        order.setOrderSide(OrderSide.SELL);
        order.setCreatedDate(LocalDate.now());

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(ArgumentMatchers.<Order>any())).thenReturn(order);

        OrderResponseDto response = orderService.createOrder(dto, username);

        assertEquals(OrderStatus.MATCHED, response.orderStatus());
    }

    @Test
    void testSellOrderWithInsufficientBalance() {
        OrderRequestDto dto = buildRequest(OrderSide.SELL, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset asset = mockAsset(assetName, 1, 1); // insufficient

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName)).thenReturn(Optional.of(asset));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.createOrder(dto, username));

        assertEquals("Insufficient asset balance", ex.getMessage());
    }

    @Test
    void testBuyOrderWithInsufficientTRY() {
        OrderRequestDto dto = buildRequest(OrderSide.BUY, price);
        Customer customer = mockCustomer();
        AssetList assetList = mockAssetList(price);
        Asset tryAsset = mockAsset("TRY", 100, 100); // insufficient

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(assetListRepository.findByAssetName(assetName)).thenReturn(Optional.of(assetList));
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")).thenReturn(Optional.of(tryAsset));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.createOrder(dto, username));

        assertEquals("Insufficient TRY balance", ex.getMessage());
    }

    @Test
    void testUnauthorizedUser() {
        OrderRequestDto dto = buildRequest(OrderSide.BUY, price);
        Customer customer = mockCustomer();
        customer.setId(999L); // farklı ID

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.createOrder(dto, username));

        assertEquals("You are not authorized to create an order for another user", ex.getMessage());
    }

    @Test
    void testInvalidOrderSide() {
        // Bu test, enum dışı bir değerle çalışmaz çünkü enum zaten sınırlı.
        // Ancak kodda default case varsa test edilebilir.
    }
}

