package com.eMartix.order_service.service.impl;

import com.eMartix.commons.advice.ResourceNotFoundException;
import com.eMartix.commons.dtos.ApiResponse;
import com.eMartix.commons.utils.AppContants;
import com.eMartix.commons.utils.CustomHeaders;
import com.eMartix.order_service.contant.PaymentMethod;
import com.eMartix.order_service.contant.Process;
import com.eMartix.order_service.dto.request.OrderBasicInfoDto;
import com.eMartix.order_service.dto.request.OrderEventDto;
import com.eMartix.order_service.dto.request.ProductDto;
import com.eMartix.order_service.dto.response.*;
import com.eMartix.order_service.entity.Order;
import com.eMartix.order_service.mapper.OrderMapper;
import com.eMartix.order_service.publisher.OrderEventPublisher;
import com.eMartix.order_service.repository.OrderRepository;
import com.eMartix.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private OrderRepository orderRepository;
    private WebClient webClient;
    private OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    private HttpServletRequest request;

    @Override
    public CreateOrderResultDto createOrder(Long userId, OrderBasicInfoDto orderBasicInfoDto) {
        List<ProductDto> listProductInCart = webClient.get()
                .uri("http://localhost:8082/api/v1/carts")
                .header(CustomHeaders.X_AUTH_USER_ID, String.valueOf(userId))
                .retrieve()
                .bodyToFlux(ProductDto.class)
                .collectList().block();

        if (listProductInCart == null || listProductInCart.isEmpty()) {
            throw new RuntimeException("Cart is empty") ;
        }

        // lay thong tin email khach hang
        ApiResponse<UserDto> user = webClient.get()
                .uri("http://localhost:5000/me")
                .header(CustomHeaders.X_API_KEY, AppContants.X_API_KEY)
//                .header(CustomHeaders.AUTHENTICATION, request.getHeader(CustomHeaders.AUTHENTICATION))
                .header(CustomHeaders.AUTHENTICATION, request.getHeader(CustomHeaders.AUTHENTICATION))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDto>>() {})
                .block();
        /**
         * lấy tổng tiền từ listProductInCart
         */
        if (user == null) {
            log.error("[Create Order] User with id [{}] null", userId);
            throw new ResourceNotFoundException("User", "Id", userId);
        }

        double totalMoney = listProductInCart.stream()
                .mapToDouble(prod -> prod.getPrice() * (1 - prod.getDiscountRate()) * prod.getQuantity()).sum();
        Order order = new Order();
        order.setUserId(userId);
        order.setTotal(totalMoney);
        order.setStatus(Process.CREATED);
        order.setPaymentMethod(PaymentMethod.valueOf(orderBasicInfoDto.getPaymentMethod().name()));
        order.setPaymentStatus("PENDING");
        // order.setShippingFee();
        order.setNote(orderBasicInfoDto.getNote());
        order.setCreatedAt(LocalDateTime.now());
        order.setAddressId(orderBasicInfoDto.getAddressId());
        var saveOrder = orderRepository.save(order);

        log.error("[Create Order] Saved order id [{}]", saveOrder.getId());
        /**
         * gọi tới payment service dê tạo session thanh toán => nhả về link thanh toán
         */
        OrderDto orderDto = orderMapper.mapToDto(saveOrder);

        CreateOrderResultDto createOrderResultDto = new CreateOrderResultDto();
        createOrderResultDto.setOrderDto(orderDto);
        createOrderResultDto.setResult("Tạo đơn hàng thành công");
        // 4. Tạo payment session nếu cần (nếu không phải COD)
        String paypalLink = null;
        if (orderBasicInfoDto.getPaymentMethod() != PaymentMethod.COD) {
            paypalLink = "http://localhost:8080/api/v1/order/" + order.getId();
        }
        if (order.getPaymentMethod().equals(PaymentMethod.COD)) {
            // sent notification đang chuẩn bị hàng
            OrderEventDto orderEventDto = OrderEventDto.builder()
                    .orderId(saveOrder.getId())
                    .email(user.getResponse().getEmail())
                    .status("CREATED")
                    .build();
            orderEventPublisher.publishOrderCreate(orderEventDto);
        }
        createOrderResultDto.setPaypalLink(paypalLink);
        return createOrderResultDto;
    }

    @Override
    public CreateOrderResultDto captureOrder(long orderId) {
        // TODO Handle Logic -> Payment Method
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new ResourceNotFoundException("Order", "id", orderId));
        if (order.getPaymentMethod() != PaymentMethod.COD) {
            order.setPaymentStatus("COMPLETE");
            order.setStatus(Process.PREPARING);
            orderRepository.save(order);
            // bắn notification thông báo thanh toán thành công
            return new CreateOrderResultDto();
        }
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
