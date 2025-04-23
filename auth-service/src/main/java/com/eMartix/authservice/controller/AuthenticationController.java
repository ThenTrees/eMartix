package com.eMartix.authservice.controller;

import com.eMartix.authservice.dto.request.*;
import com.eMartix.authservice.dto.response.LoginResponse;
import com.eMartix.authservice.dto.response.UserResponseDto;
import com.eMartix.authservice.service.AuthenticationService;
import com.eMartix.commons.dtos.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestDto loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = authenticationService.authenticateUser(loginRequest, response);
        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .code(HttpStatus.OK.value())
                        .success(true)
                        .message("Login successful")
                        .data(loginResponse)
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        UserResponseDto user = authenticationService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponseDto>builder()
                        .code(HttpStatus.CREATED.value())
                        .success(true)
                        .message("User registered successfully")
                        .data(user)
                        .build());
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refreshing token");
        // Lấy tất cả cookies từ request
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Kiểm tra xem có cookie nào tên là "refreshToken"
                if ("refreshToken".equals(cookie.getName())) {
                    // Trả về giá trị của refreshToken
                    refreshToken = cookie.getValue();
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.createRefreshToken(refreshToken, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        String logoutResponse = authenticationService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(
                    ApiResponse.builder()
                            .code(HttpStatus.NO_CONTENT.value())
                            .success(true)
                            .message("Logout successful")
                            .data(logoutResponse)
                            .build()
                );
    }

    @PostMapping("send-verification-otp")
    public ResponseEntity<ApiResponse<?>> sendVerificationEmail(@RequestBody VerifyOtpRequestDto requestDto) {
        boolean rs = authenticationService.verifyEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .success(true)
                .message("Verification email sent")
                .data(rs)
                .build());
    }

//     forgot password
    @PostMapping("reset-password-request")
    public ResponseEntity<ApiResponse<?>> sendResetPasswordEmail(@RequestBody() ForgotPasswordRequestDto request) {
        authenticationService.sentRequireForgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .success(true)
                        .message("Pls check your email to confirm reset password")
                        .data(null)
                        .build());
    }

    @GetMapping("/resent-otp")
    public ResponseEntity<ApiResponse<?>> resentOtp(@RequestBody ResentOtpRequestDto requestDto) {
        authenticationService.resentOtp(requestDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .success(true)
                        .message("Resent OTP successfully")
                        .data(null)
                        .build());
    }

    @GetMapping("/verify-link")
    public ResponseEntity<ApiResponse<?>> verifyLink(@RequestParam("token") String token,
                                                     @RequestParam("username") String username,
                                                     @RequestParam("password") String password) {
        authenticationService.verifyLink(username, token, password);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .success(true)
                        .message("Verify link successfully")
                        .data(null)
                        .build());
    }
}
