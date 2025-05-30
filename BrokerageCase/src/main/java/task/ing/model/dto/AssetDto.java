package task.ing.model.dto;

import java.math.BigDecimal;

public record AssetDto(

        String assetName,

        double size,

        double usableSize,

        BigDecimal currentPrice


) {

}
