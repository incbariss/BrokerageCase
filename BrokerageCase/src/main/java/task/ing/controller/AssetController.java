package task.ing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.AssetCreateRequestDto;
import task.ing.model.dto.request.AssetUpdateRequestDto;
import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.dto.response.CustomerAssetsResponseDto;
import task.ing.service.AssetService;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Customer's Asset Operations", description = "asset-controller")
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/get/{customerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Both USER and ADMIN can access",
            description = "Customers can see their own assets")
    public ResponseEntity<List<AssetResponseDto>> getAssetsByCustomer(
            @PathVariable @Positive(message = "Customer ID must be a positive number") Long customerId,
            Authentication authentication) {

        String currentUsername = authentication.getName();

        List<AssetResponseDto> assets = assetService.getAssetsByCustomerId(customerId, currentUsername);
        return ResponseEntity.ok(assets);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all-assets")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can see all the customers and their assets")
    public ResponseEntity<List<CustomerAssetsResponseDto>> getAllAssets() {
        List<CustomerAssetsResponseDto> allAssets = assetService.getAllAssetsGroupedByCustomer();
        return ResponseEntity.ok(allAssets);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can add a new asset to any customer")
    public ResponseEntity<AssetResponseDto> addAsset(@Valid @RequestBody AssetCreateRequestDto dto) {
        AssetResponseDto response = assetService.addAssetToCustomer(dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can update any customer's asset")
    public ResponseEntity<AssetResponseDto> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequestDto dto) {
        AssetResponseDto response = assetService.updateAsset(id, dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can delete any customer's asset")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.softDeleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/restore/{id}")
    @Operation(
            summary = "Only ADMIN can access",
            description = "Admin can restore any customer's asset")
    public ResponseEntity<Void> restoreAsset(@PathVariable Long id) {
        assetService.restoreAsset(id);
        return ResponseEntity.noContent().build();
    }


}
