package task.ing.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task.ing.model.dto.response.AssetResponseDto;
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
}
