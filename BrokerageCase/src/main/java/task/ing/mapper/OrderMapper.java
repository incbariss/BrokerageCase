package task.ing.mapper;

import task.ing.model.dto.request.OrderRequestDto;
import task.ing.model.dto.response.OrderResponseDto;
import task.ing.model.entity.AssetList;
import task.ing.model.entity.Customer;
import task.ing.model.entity.Order;
import task.ing.model.enums.OrderStatus;

public interface OrderMapper {

    static Order toEntity(OrderRequestDto dto, AssetList assetList, Customer customer) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(dto.assetName());
        order.setSize(dto.size());
        order.setPrice(dto.price());
        order.setOrderSide(dto.orderSide());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setAssetList(assetList);
        return order;
    }

    static OrderResponseDto toDto(Order order) {
        return new OrderResponseDto(
//                order.getCustomer().getId(),
                order.getAssetName(),
                order.getPrice(),
                order.getSize(),
                order.getCreatedDate(),
                order.getOrderSide(),
                order.getOrderStatus()
        );
    }

}
