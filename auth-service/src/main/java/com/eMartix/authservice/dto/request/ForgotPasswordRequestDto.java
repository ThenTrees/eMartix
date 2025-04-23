package com.eMartix.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequestDto {
    private String usernameOrEmail;
    private String newPassword;
    private String confirmPassword;
}
