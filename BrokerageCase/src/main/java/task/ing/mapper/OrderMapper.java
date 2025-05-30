package task.ing.mapper;

import task.ing.model.dto.OrderDto;
import task.ing.model.entity.AssetList;
import task.ing.model.entity.Order;
import task.ing.model.enums.OrderStatus;

import java.time.LocalDateTime;

public interface OrderMapper {

    static Order toEntity(OrderDto dto, AssetList assetList) {
        Order order = new Order();
        order.setCustomerId(dto.customerId());
        order.setAssetName(dto.assetName());
        order.setSize(dto.size());
        order.setPrice(dto.price());
        order.setOrderSide(dto.orderSide());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        order.setAssetList(assetList);
        return order;
    }

    static OrderDto toDto(Order order) {
        return new OrderDto(
                order.getCustomerId(),
                order.getAssetName(),
                order.getPrice(),
                order.getSize(),
                order.getOrderSide()
                );
    }

}
