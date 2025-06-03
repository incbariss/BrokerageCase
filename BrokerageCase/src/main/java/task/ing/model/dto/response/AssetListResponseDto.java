package task.ing.model.dto.response;

import java.math.BigDecimal;

public record AssetListResponseDto(
        Long id,

        String assetName,

        String assetFullName,

        BigDecimal currentPrice
) {
}
