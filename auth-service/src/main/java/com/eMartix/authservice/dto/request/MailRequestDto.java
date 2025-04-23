package com.eMartix.authservice.dto.request;

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
