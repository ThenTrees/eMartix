package com.eMartix.noti_service.noti.service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private String message;
    private String title;
    private LocalDateTime timestamp;
}
