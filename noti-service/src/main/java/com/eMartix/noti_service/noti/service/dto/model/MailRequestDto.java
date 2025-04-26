package com.eMartix.noti_service.noti.service.dto.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestDto {
    private String to;
    private String subject;
    private String body;
}
