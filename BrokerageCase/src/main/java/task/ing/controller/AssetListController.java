package task.ing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task.ing.model.dto.request.AssetListRequestDto;
import task.ing.model.dto.response.AssetListResponseDto;
import task.ing.model.entity.Asset;
import task.ing.service.AssetListService;

import java.util.List;

@RestController
@RequestMapping("/api/assetlist")
@RequiredArgsConstructor
public class AssetListController {

    private final AssetListService assetListService;

    @GetMapping
    public ResponseEntity<List<AssetListResponseDto>> getAssetList() {
        List<AssetListResponseDto> assetlist = assetListService.getAssetList();
        return ResponseEntity.ok(assetlist);
    }

    @PostMapping
    public ResponseEntity<AssetListResponseDto> addAsset (
            @Valid @RequestBody AssetListRequestDto requestDto) {
        AssetListResponseDto responseDto = assetListService.addAsset(requestDto);
        return ResponseEntity.ok(responseDto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<AssetListResponseDto> updateAsset (
            @PathVariable Long id,
            @Valid @RequestBody AssetListRequestDto requestDto) {
        AssetListResponseDto responseDto = assetListService.updateAsset(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetListService.softDeleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restoreAsset(@PathVariable Long id) {
        assetListService.restoreAsset(id);
        return ResponseEntity.noContent().build();
    }

}
