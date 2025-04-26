package com.eMartix.order_service.service;

import com.eMartix.order_service.dto.request.OrderBasicInfoDto;
import com.eMartix.order_service.dto.response.CancelOrderResult;
import com.eMartix.order_service.dto.response.CreateOrderResultDto;
import com.eMartix.order_service.dto.response.ListOrderDto;
import com.eMartix.order_service.dto.response.OrderDto;

public interface OrderService {
    CreateOrderResultDto createOrder(long userId, OrderBasicInfoDto orderBasicInfoDto);
    CreateOrderResultDto captureOrder(long orderId);
    CancelOrderResult cancelCapture(long orderId);
    ListOrderDto getAllOrder(int pageNo, int pageSize, String sortBy, String sortDir);
    OrderDto getOrderById(long id);
    OrderDto updateOrder(OrderDto orderDto, long id);
    String cancelOrderById(long id);

}
