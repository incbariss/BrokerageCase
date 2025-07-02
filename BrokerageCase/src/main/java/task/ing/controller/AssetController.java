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


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    @Operation(
            summary = "USER",
            description = "Customers can see their own assets")
    public ResponseEntity<List<AssetResponseDto>> getMyAssets(Authentication authentication) {
        String currentUsername = authentication.getName();
        List<AssetResponseDto> assets = assetService.getAssetsForCurrentUser(currentUsername);
        return ResponseEntity.ok(assets);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    @Operation(
            summary = "ADMIN",
            description = "Admin can see all the customers and their assets")
    public ResponseEntity<List<CustomerAssetsResponseDto>> getAllAssets() {
        List<CustomerAssetsResponseDto> allAssets = assetService.adminGetAllAssetsGroupedByCustomer();
        return ResponseEntity.ok(allAssets);
    }


}
