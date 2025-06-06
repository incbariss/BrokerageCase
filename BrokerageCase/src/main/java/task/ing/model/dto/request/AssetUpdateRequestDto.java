package task.ing.model.dto.request;

public record AssetUpdateRequestDto(

//        @NotNull(message = "Customer ID cannot be blank")
//        Long customerId

        double size,

        double usableSize
) {
}
