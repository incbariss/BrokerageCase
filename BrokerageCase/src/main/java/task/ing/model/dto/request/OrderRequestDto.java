package task.ing.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import task.ing.model.enums.OrderSide;

import java.math.BigDecimal;

public record OrderRequestDto(

//        @NotNull(message = "Customer ID cannot be blank")
//        Long customerId,

        @NotBlank(message = "Asset name cannot be blank")
        @Pattern(regexp = "^[A-Z]{2,5}$", message = "Asset name must be uppercase letters only")
        String assetName,

        @NotNull(message = "Price cannot be blank")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Size cannot be blank")
        @DecimalMin(value = "0.001", message = "Size must be greater than 0")
        double size,

        @NotNull(message = "Order side (BUY, SELL) cannot be blank")
        OrderSide orderSide


) {
}
