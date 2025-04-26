package com.eMartix.noti_service.noti.service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotiRequestDto {
    @NotEmpty(message = "Message should not be empty")
    @Size(min = 10, message = "Notification message should have at least 2 characters!")
    private String message;

    @NotEmpty(message = "Title should not be empty")
    @Size(min = 2, message = "Notification title should have at least 2 characters!")
    private String title;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
