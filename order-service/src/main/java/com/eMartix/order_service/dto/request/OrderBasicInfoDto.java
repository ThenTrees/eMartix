package com.eMartix.order_service.dto.request;

import com.eMartix.order_service.contant.PaymentMethod;
import lombok.Data;

@Data
public class OrderBasicInfoDto {
    private long addressId;
    private String note;
    private PaymentMethod paymentMethod;
    private String returnUrl;
    private String cancelUrl;
}
