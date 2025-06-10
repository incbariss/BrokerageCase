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

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "While logged-in customers can update their own info, admin can update the information of any customer.")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequestDto dto,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        CustomerResponseDto updated = customerService.updateCustomer(id, dto, currentUsername);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can delete any customer.")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.softDeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/restore/{id}")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can restore any customer.")
    public ResponseEntity<Void> restoreCustomer(@PathVariable Long id) {
        customerService.restoreCustomer(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "While logged-in customers can only get their own info, admin can get the information of any customer.")
    public ResponseEntity<CustomerResponseDto> getCustomerById(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        CustomerResponseDto dto = customerService.getCustomerById(id, currentUsername);
        return ResponseEntity.ok(dto);
    }


}
