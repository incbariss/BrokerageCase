package task.ing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.CustomerRequestDto;
import task.ing.model.dto.response.CustomerAssetsResponseDto;
import task.ing.model.dto.response.CustomerResponseDto;
import task.ing.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@RequestBody @Valid CustomerRequestDto dto) {
        CustomerResponseDto responseDto = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer (
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequestDto dto) {
        CustomerResponseDto updated = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.softDeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restoreCustomer(@PathVariable Long id) {
        customerService.restoreCustomer(id);
        return ResponseEntity.ok().build();
    }

}
