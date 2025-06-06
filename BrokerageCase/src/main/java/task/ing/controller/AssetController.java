package task.ing.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.AssetCreateRequestDto;
import task.ing.model.dto.request.AssetUpdateRequestDto;
import task.ing.model.dto.response.AssetListResponseDto;
import task.ing.model.dto.response.AssetResponseDto;
import task.ing.model.dto.response.CustomerAssetsResponseDto;
import task.ing.service.AssetService;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AssetResponseDto>> getAssetsByCustomer(
            @PathVariable @Positive(message = "Customer ID must be a positive number") Long customerId) {
        List<AssetResponseDto> assets = assetService.getAssetsByCustomerId(customerId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/getAllAssets")
    public ResponseEntity<List<CustomerAssetsResponseDto>> getAllAssets() {
        List<CustomerAssetsResponseDto> allAssets = assetService.getAllAssetsGroupedByCustomer();
        return ResponseEntity.ok(allAssets);
    }

    @PostMapping("/add")
    public ResponseEntity<AssetResponseDto> addAsset(@Valid @RequestBody AssetCreateRequestDto dto) {
        AssetResponseDto response = assetService.addAssetToCustomer(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AssetResponseDto> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequestDto dto) {
        AssetResponseDto response = assetService.updateAsset(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.softDeleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id) {
        assetService.restoreAsset(id);
        return ResponseEntity.noContent().build();
    }




}
