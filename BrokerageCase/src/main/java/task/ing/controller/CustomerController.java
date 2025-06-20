package task.ing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.CustomerRequestDto;
import task.ing.model.dto.response.CustomerResponseDto;
import task.ing.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Operations", description = "customer-controller")
public class CustomerController {

    private final CustomerService customerService;

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "ADMIN",
            description = "Admin can update customer's info")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequestDto dto,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        CustomerResponseDto updated = customerService.adminUpdateCustomer(id, dto, currentUsername);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "USER",
            description = "Customers can update their own info")
    public ResponseEntity<CustomerResponseDto> updateCustomerInfo(
            @RequestBody @Valid CustomerRequestDto dto,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        CustomerResponseDto updated = customerService.updateCurrentCustomer(dto, currentUsername);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "ADMIN",
            description = "Admin can get customer's information by ID")
    public ResponseEntity<CustomerResponseDto> getCustomerById(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        CustomerResponseDto dto = customerService.getCustomerById(id, currentUsername);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "USER",
            description = "Customers can get their own info")
    public ResponseEntity<CustomerResponseDto> getCustomerInfo(Authentication authentication) {

        String currentUsername = authentication.getName();
        CustomerResponseDto dto = customerService.getCurrentCustomer(currentUsername);
        return ResponseEntity.ok(dto);
    }



}
