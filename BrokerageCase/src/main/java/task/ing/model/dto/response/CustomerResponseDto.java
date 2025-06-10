package task.ing.model.dto.response;

public record CustomerResponseDto(

        Long id,

        String name,

        String surname,

        String username,

        String email
) {
}
