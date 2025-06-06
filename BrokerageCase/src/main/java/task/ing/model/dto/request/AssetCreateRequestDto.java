package task.ing.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AssetCreateRequestDto(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "AssetList ID is required")
        Long assetListId,

        @DecimalMin(value = "0.0", inclusive = true, message = "Size must be greater than 0")
        double size
) {
}
