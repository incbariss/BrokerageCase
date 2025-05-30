package task.ing.model.dto;

import task.ing.model.enums.OrderSide;

import java.math.BigDecimal;

public record OrderDto(

        Long customerId,

        String assetName,

        BigDecimal price,

        double size,

        OrderSide orderSide


) {
}
