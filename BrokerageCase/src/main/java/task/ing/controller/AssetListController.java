package task.ing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.AssetListRequestDto;
import task.ing.model.dto.response.AssetListResponseDto;
import task.ing.service.AssetListService;

import java.util.List;

@RestController
@RequestMapping("/api/assetlist")
@RequiredArgsConstructor
@Tag(name = "AssetList Operations", description = "assetlist-controller")
public class AssetListController {

    private final AssetListService assetListService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/all")
    @Operation(
            summary = "USER",
            description = "Customers can see all the asset from the assetlist")
    public ResponseEntity<List<AssetListResponseDto>> getAssetList() {
        List<AssetListResponseDto> assetlist = assetListService.getAssetList();
        return ResponseEntity.ok(assetlist);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    @Operation(
            summary = "ADMIN",
            description = "Admin can add a new asset to the assetlist")
    public ResponseEntity<AssetListResponseDto> addAsset(
            @Valid @RequestBody AssetListRequestDto requestDto) {
        AssetListResponseDto responseDto = assetListService.addAsset(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    @Operation(
            summary = "ADMIN",
            description = "Admin can update any asset from the assetlist")
    public ResponseEntity<AssetListResponseDto> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetListRequestDto requestDto) {
        AssetListResponseDto responseDto = assetListService.updateAsset(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    @Operation(
            summary = "ADMIN",
            description = "Admin can delete any asset from the assetlist")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetListService.softDeleteAsset(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/{id}")
    @Operation(
            summary = "ADMIN",
            description = "Admin can restore any asset from the assetlist")
    public ResponseEntity<Void> restoreAsset(@PathVariable Long id) {
        assetListService.restoreAsset(id);
        return ResponseEntity.noContent().build();
    }

}
