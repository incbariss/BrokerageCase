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
    @PostMapping
    @Operation(
            summary = "USER",
            description = "Customers can place orders at their desired prices")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto requestDto,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        OrderResponseDto responseDto = orderService.createOrder(requestDto, currentUsername);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/my-orders")
    @Operation(
            summary = "USER",
            description = "Customers can view their orders by applying a date filter.")
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        List<OrderResponseDto> orders = orderService.listOrdersForCurrentUser(start, end, currentUsername);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    @Operation(
            summary = "ADMIN",
            description = "Admin can list customer orders by filtering date")
    public ResponseEntity<List<OrderResponseDto>> listOrdersForCustomer(
            @RequestParam @Positive(message = "Customer ID must be positive") Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<OrderResponseDto> orders = orderService.listOrdersForAdmin(customerId, start, end);
        return ResponseEntity.ok(orders);
    }



    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{orderId}")
    @Operation(
            summary = "USER",
            description = "Customers can cancel their orders (Only Pending orders can be cancelled)")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable @Positive(message = "Order ID must be positive") Long orderId,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        orderService.cancelOrder(orderId, currentUsername);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/deposit")
    @Operation(
            summary = "USER",
            description = "Customers can deposit TRY before creating a new order")
    public ResponseEntity<OrderResponseDto> depositTRYForCurrentUser(
            @RequestParam double amount,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        OrderResponseDto responseDto = orderService.depositForCurrentUser(amount, currentUsername);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/withdraw")
    @Operation(
            summary = "USER",
            description = "Customers can withdraw TRY from their account.")
    public ResponseEntity<OrderResponseDto> withdrawTRYForCurrentUser(
            @RequestParam double amount,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        OrderResponseDto responseDto = orderService.withdrawForCurrentUser(amount, currentUsername);
        return ResponseEntity.ok(responseDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/match")
    @Operation(
            summary = "ADMIN",
            description = "Admin can match the pending orders. (The asset list will reflect the most recent accepted order as the current price)")
    public ResponseEntity<String> approveMatchOrders(
            @RequestParam Long buyOrderId,
            @RequestParam Long sellOrderId) {
        orderService.approveMatchedOrders(buyOrderId, sellOrderId);
        return ResponseEntity.ok("Orders matched and asset list current price updated successfully");
    }

}
