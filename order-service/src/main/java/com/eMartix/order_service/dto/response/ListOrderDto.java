package com.eMartix.order_service.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ListOrderDto {
    private List<OrderDto> orderList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
