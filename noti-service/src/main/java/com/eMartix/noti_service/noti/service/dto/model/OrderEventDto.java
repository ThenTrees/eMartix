package com.eMartix.noti_service.noti.service.dto.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class OrderEventDto {
    private Long orderId;
    private String email;
    private String status; // CREATED, PAID, CANCELLED
    @CreationTimestamp
    private LocalDateTime timestamp;
}
