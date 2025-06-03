package task.ing.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequestDto(

        @NotNull(message = "Customer ID cannot be blank")
        Long customerId
) {
}
