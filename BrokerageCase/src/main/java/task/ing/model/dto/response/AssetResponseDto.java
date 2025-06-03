package task.ing.model.dto.response;

import java.math.BigDecimal;

public record AssetResponseDto(

        String assetName,

        double size,

        double usableSize,

        BigDecimal currentPrice


) {

}
