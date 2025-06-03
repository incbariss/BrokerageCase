package task.ing.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AssetListRequestDto(

        @NotBlank(message = "Asset name cannot be blank")
        @Pattern(regexp = "^[A-Z]{2,10}$", message = "Asset name must be uppercase letters only")
        String assetName,

        @NotBlank(message = "Asset full name cannot be blank")
        String assetFullName,

        @NotNull(message = "Current price cannot be blank")
        @DecimalMin(value = "0.01", message = "Current price must be greater than 0")
        BigDecimal currentPrice
) {
}
