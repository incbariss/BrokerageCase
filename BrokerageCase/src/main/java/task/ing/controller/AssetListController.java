package task.ing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task.ing.model.dto.response.AssetListResponseDto;
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
}
