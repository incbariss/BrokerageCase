package task.ing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.OrderRequestDto;
import task.ing.model.dto.response.OrderResponseDto;
import task.ing.service.OrderService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Operations", description = "order-controller")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "Customers can place orders at their desired prices")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        OrderResponseDto responseDto = orderService.createOrder(requestDto, currentUsername);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/list")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "Customers can view their orders by applying a date filter.")
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam @Positive(message = "Customer ID must be positive") Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        List<OrderResponseDto> orders = orderService.listOrders(customerId, start, end, currentUsername);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/cancel/{orderId}")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "Customers can cancel their orders (Only Pending orders can be cancelled)")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable @Positive(message = "Order ID must be positive") Long orderId,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        orderService.cancelOrder(orderId, currentUsername);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/depositTRY")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "Customers can deposit TRY before creating a new order")
    public ResponseEntity<OrderResponseDto> depositTRY(
            @RequestParam Long customerId,
            @RequestParam double amount,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        OrderResponseDto responseDto = orderService.depositTRY(customerId, amount, currentUsername);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/withdrawTRY")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "Customers can withdraw TRY from their account")
    public ResponseEntity<OrderResponseDto> withdrawTRY(
            @RequestParam Long customerId,
            @RequestParam double amount,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        OrderResponseDto responseDto = orderService.withdrawTRY(customerId, amount, currentUsername);
        return ResponseEntity.ok(responseDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/match-orders")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can match the pending orders. (The asset list will reflect the most recent accepted order as the current price)")
    public ResponseEntity<String> approveMatchOrders(
            @RequestParam Long buyOrderId,
            @RequestParam Long sellOrderId) {
        orderService.approveMatchedOrders(buyOrderId, sellOrderId);
        return ResponseEntity.ok("Orders matched and asset list current price updated successfully");
    }

}
