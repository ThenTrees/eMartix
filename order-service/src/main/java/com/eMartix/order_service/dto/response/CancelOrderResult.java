package com.eMartix.order_service.dto.response;

import lombok.Data;

@Data
public class CancelOrderResult {
    String message;
    boolean isSuccess;
}
