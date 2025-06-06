package task.ing.model.dto.response;

import java.util.List;

public record CustomerAssetsResponseDto(
        Long customerId,

        String customerName,

        List<AssetResponseDto> assets
) {
}
