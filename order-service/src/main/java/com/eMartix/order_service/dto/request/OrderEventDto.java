package com.eMartix.order_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEventDto implements Serializable {
    private Long orderId;
    private String email;
    private String status; // CREATED, PAID, CANCELLED
    @CreationTimestamp
    private LocalDateTime timestamp;
}
