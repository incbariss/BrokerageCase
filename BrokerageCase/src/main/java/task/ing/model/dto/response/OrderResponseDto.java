package task.ing.model.dto.response;

import task.ing.model.enums.OrderSide;
import task.ing.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderResponseDto(

        Long customerId,

        String assetName,

        BigDecimal price,

        double size,

        LocalDate createdDate,

        OrderSide orderSide,

        OrderStatus orderStatus

) {
}
