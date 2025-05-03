package com.eMartix.noti_service.noti.service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private String message;
    private String title;
    private LocalDateTime timestamp;
}
