package com.eMartix.authservice.service;

import com.eMartix.authservice.dto.request.*;
import com.eMartix.authservice.dto.response.LoginResponse;
import com.eMartix.authservice.dto.response.UserResponseDto;
import com.eMartix.commons.dtos.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    LoginResponse authenticateUser(LoginRequestDto request, HttpServletResponse response);

    ApiResponse<LoginResponse> createRefreshToken(String username,HttpServletResponse response);

    String logout(HttpServletRequest request, HttpServletResponse response);

    UserResponseDto register(RegisterRequestDto request);

    boolean verifyEmail(VerifyOtpRequestDto requestDto);

    void sentRequireForgotPassword(ForgotPasswordRequestDto requestDto);

    void resentOtp(ResentOtpRequestDto username);
    void verifyLink(String username, String token, String password);
}
