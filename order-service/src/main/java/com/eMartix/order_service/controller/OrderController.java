package com.eMartix.order_service.controller;

import com.eMartix.commons.utils.AppContants;
import com.eMartix.commons.utils.CustomHeaders;
import com.eMartix.order_service.dto.request.OrderBasicInfoDto;
import com.eMartix.order_service.dto.response.CancelOrderResult;
import com.eMartix.order_service.dto.response.CreateOrderResultDto;
import com.eMartix.order_service.dto.response.ListOrderDto;
import com.eMartix.order_service.dto.response.OrderDto;
import com.eMartix.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @PostMapping
    public ResponseEntity<CreateOrderResultDto> createOrder(@RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId,
                                                            @RequestBody OrderBasicInfoDto orderBasicInfoDto) {
        logger.info("create order with userId: {} and orderBasicInfoDto: {}", userId, orderBasicInfoDto);
        return new ResponseEntity<>(orderService.createOrder(userId, orderBasicInfoDto), HttpStatus.OK);
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<CreateOrderResultDto> captureOrder(@PathVariable long orderId) {
//        TODO handle logic -> Payment method

        return ResponseEntity.ok(new CreateOrderResultDto());
    }
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable long orderId, @RequestBody @Valid OrderDto orderDto){
        return new ResponseEntity<>(orderService.updateOrder(orderDto, orderId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ListOrderDto> getAllOrder(
            @RequestParam(value = "pageNo", defaultValue = AppContants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppContants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppContants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppContants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ){
        return new ResponseEntity<>(orderService.getAllOrder(pageNo, pageSize, sortBy, sortDir), HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable long orderId){
        return new ResponseEntity<>(orderService.getOrderById(orderId), HttpStatus.OK);
    }


    @DeleteMapping("/")
    public ResponseEntity<CancelOrderResult> deleteCaptureOrder(@PathVariable long orderId) {
        // TODO Handle Logic - > Payment Method
        return ResponseEntity.ok(new CancelOrderResult());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable long orderId) {
//        TODO Handle Logic -> Payment Method
        return new ResponseEntity<>(orderService.cancelOrderById(orderId), HttpStatus.OK);
    }

}
