package task.ing.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(

        @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,


        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
        String password
) {
}
