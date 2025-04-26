package com.eMartix.order_service.mapper;

import com.eMartix.order_service.dto.response.OrderDto;
import com.eMartix.order_service.entity.Order;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderMapper {

    private ModelMapper mapper;

    public OrderDto mapToDto(Order order){
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        return orderDto;
    }

    public Order mapToEntity(OrderDto orderDto){
        Order order = mapper.map(orderDto, Order.class);
        return order;
    }
}

