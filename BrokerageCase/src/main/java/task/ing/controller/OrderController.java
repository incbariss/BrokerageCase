package task.ing.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.OrderRequestDto;
import task.ing.model.dto.response.OrderResponseDto;
import task.ing.service.OrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto requestDto) {
        OrderResponseDto responseDto = orderService.createOrder(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam @Positive(message = "Customer ID must be positive") Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<OrderResponseDto> orders = orderService.listOrders(customerId, start, end);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable @Positive(message = "Order ID must be positive")  Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/depositTRY")
    public ResponseEntity<OrderResponseDto> depositTRY(
            @RequestParam Long customerId,
            @RequestParam double amount) {
        OrderResponseDto responseDto = orderService.depositTRY(customerId, amount);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/approve-match")
    public ResponseEntity<String> approveMatchOrders(
            @RequestParam Long buyOrderId,
            @RequestParam Long sellOrderId) {
        orderService.approveMatchedOrders(buyOrderId, sellOrderId);
        return ResponseEntity.ok("Orders matched and asset price updated successfully");
    }

}
