package task.ing.model.dto.request;

import jakarta.validation.constraints.Min;

public record AssetUpdateRequestDto(

        @Min(value = 0, message = "Size must be at least 0")
        double size,

        @Min(value = 0, message = "Usable size must be at least 0")
        double usableSize
) {
}
