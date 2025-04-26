package com.eMartix.order_service.dto.request;

import lombok.Data;

@Data
public class ProductOptionDto {
    private Long id;

    private String name;
    private String value;
}
