package task.ing.mapper;

import task.ing.model.dto.request.CustomerRequestDto;
import task.ing.model.dto.response.CustomerResponseDto;
import task.ing.model.entity.Customer;

public interface CustomerMapper {

    static Customer toEntity(CustomerRequestDto dto) {
        Customer customer = new Customer();
        customer.setName(dto.name());
        customer.setSurname(dto.surname());
        customer.setEmail(dto.email());
        customer.setUsername(dto.username());
        customer.setPassword(dto.password());
        return customer;
    }


    static CustomerResponseDto toDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getName(),
                customer.getUsername()
        );
    }
}
