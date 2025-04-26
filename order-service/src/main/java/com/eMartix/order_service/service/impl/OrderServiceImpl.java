package com.eMartix.order_service.service.impl;

import com.eMartix.commons.advice.ResourceNotFoundException;
import com.eMartix.commons.utils.CustomHeaders;
import com.eMartix.order_service.contant.PaymentMethod;
import com.eMartix.order_service.contant.Process;
import com.eMartix.order_service.dto.request.OrderBasicInfoDto;
import com.eMartix.order_service.dto.request.ProductDto;
import com.eMartix.order_service.dto.response.CancelOrderResult;
import com.eMartix.order_service.dto.response.CreateOrderResultDto;
import com.eMartix.order_service.dto.response.ListOrderDto;
import com.eMartix.order_service.dto.response.OrderDto;
import com.eMartix.order_service.entity.Order;
import com.eMartix.order_service.mapper.OrderMapper;
import com.eMartix.order_service.repository.OrderRepository;
import com.eMartix.order_service.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private OrderRepository orderRepository;
    private WebClient webClient;
    private OrderMapper orderMapper;

    @Override
    public CreateOrderResultDto createOrder(long userId, OrderBasicInfoDto orderBasicInfoDto) {
        List<ProductDto> listProductInCart = webClient.get()
                .uri("http://localhost:8082/api/v1/carts")
                .header(CustomHeaders.X_AUTH_USER_ID, String.valueOf(userId))
                .retrieve()
                .bodyToFlux(ProductDto.class)
                .collectList().block();
//  Handle Logic
            return null;

    }

    @Override
    public CreateOrderResultDto captureOrder(long orderId) {
        return null;
    }

    @Override
    public CancelOrderResult cancelCapture (long orderId) {
        return null;
    }

    @Override
    public ListOrderDto getAllOrder(int pageNo, int pageSize, String sortBy, String sortDir) {
        Page<Order> page = orderRepository.findAll(PageRequest.of(pageNo, pageSize,
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending()));
        ListOrderDto lod = new ListOrderDto();
        lod.setOrderList(page.getContent().stream()
                .map(order -> orderMapper.mapToDto(order))
                .collect(Collectors.toList()));
        lod.setLast(page.isLast());
        lod.setPageNo(page.getNumber());
        lod.setPageSize(page.getSize());
        lod.setTotalElements(page.getTotalElements());
        lod.setTotalPages(page.getTotalPages());
        return lod;
    }

    @Override
    public OrderDto getOrderById(long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.mapToDto(order);
    }

    @Override
    public OrderDto updateOrder(OrderDto orderDto, long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        order.setTotal(orderDto.getTotal());
        order.setStatus(Process.valueOf(orderDto.getStatus()));
        order.setPaymentMethod(PaymentMethod.valueOf(orderDto.getPaymentMethod()));
        order.setPaymentStatus(orderDto.getPaymentStatus());
        order.setShippingFee(orderDto.getShippingFee());
        order.setNote(orderDto.getNote());
        order.setCreatedAt(orderDto.getCreatedAt());
        order.setAddressId(orderDto.getAddressId());
        order.setUserId(orderDto.getUserId());
        // Id Payment Methods : Example: Paypal, Momo, ZaloPay, VNPay
        Order orderResult = orderRepository.save(order);
        return orderMapper.mapToDto(orderResult);
    }

    @Override
    public String cancelOrderById(long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        order.setStatus(Process.CANCELED);
        Order orderResult = orderRepository.save(order);
        return "Order canceled successfully";
    }
}
