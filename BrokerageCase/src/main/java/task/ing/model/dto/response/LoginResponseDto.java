package task.ing.model.dto.response;

public record LoginResponseDto(
        String token,

        Long id,

        String username,

        String role
) {
}
