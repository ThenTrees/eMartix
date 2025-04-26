package com.eMartix.authservice.service.impl;

import com.eMartix.authservice.common.UserStatus;
import com.eMartix.authservice.dto.request.*;
import com.eMartix.authservice.dto.response.LoginResponse;
import com.eMartix.authservice.dto.response.UserResponseDto;
import com.eMartix.authservice.helper.JwtTokenProvider;
import com.eMartix.authservice.producer.MailProducer;
import com.eMartix.authservice.model.Role;
import com.eMartix.authservice.model.User;
import com.eMartix.authservice.model.UserRole;
import com.eMartix.authservice.repository.RoleRepository;
import com.eMartix.authservice.repository.UserRepository;
import com.eMartix.authservice.repository.UserRoleRepository;
import com.eMartix.authservice.service.*;
import com.eMartix.authservice.util.GenerateRandomOTP;
import com.eMartix.commons.advice.ResourceNotFoundException;
import com.eMartix.commons.dtos.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final MailProducer mailProducer;

    @Override
    public LoginResponse authenticateUser(LoginRequestDto request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);  // Chỉ có thể truy cập từ server, bảo vệ khỏi XSS
        refreshTokenCookie.setSecure(true);    // Chỉ gửi qua HTTPS
        refreshTokenCookie.setPath("/");       // Gửi trong các yêu cầu tới toàn bộ ứng dụng
        response.addCookie(refreshTokenCookie);
        tokenService.storeTokenWithExpiry(authentication.getName(), jwt, refreshToken);

        return LoginResponse.builder().accessToken(jwt).refreshToken(refreshToken).build();

    }


    @Override
    public ApiResponse<LoginResponse> createRefreshToken(String token, HttpServletResponse response) {
        // phan giai claims -> lay sub
        String username = tokenProvider.getUsernameFromToken(token);
        // Kiểm tra Refresh Token trong Redis
        String storedToken = tokenService.getRefreshToken(username);
        if (storedToken != null && storedToken.equals(token)) {
            UserDetails exitsUser = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(exitsUser, null, exitsUser.getAuthorities());
            // Nếu tồn tại, tạo mới Refresh Token và Access Token
            String newAccessToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
            Cookie refreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
            refreshTokenCookie.setHttpOnly(true);  // Chỉ có thể truy cập từ server, bảo vệ khỏi XSS
            refreshTokenCookie.setSecure(true);    // Chỉ gửi qua HTTPS
            refreshTokenCookie.setPath("/");       // Gửi trong các yêu cầu tới toàn bộ ứng dụng
            response.addCookie(refreshTokenCookie);
            // Lưu Refresh Token mới vào Redis
            tokenService.storeTokenWithExpiry(username, newAccessToken, newRefreshToken);
            return ApiResponse.<LoginResponse>builder()
                    .code(HttpStatus.CREATED.value())
                    .message("Refresh token successfully")
                    .success(true)
                    .response(LoginResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build())
                    .build();
        }
        throw new JwtException("Refresh token is invalid");
    }


    @Transactional
    public UserResponseDto register(RegisterRequestDto registerRequest) {
        // Check if username is taken
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        // Check if email is taken
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        // Check if email is taken
        if (userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new IllegalArgumentException("phone is already in use!");
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(UserStatus.INACTIVE)
                .dateOfBirth(registerRequest.getDateOfBirth())
                .phone(registerRequest.getPhone())
                .gender(registerRequest.getGender())
                .type(registerRequest.getType())
                .build();

        User savedUser = userRepository.save(user);

        // Assign roles to user
        List<String> roleNames = registerRequest.getRoles();
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = Collections.singletonList("USER"); // Default role
        }

        for (String roleName : roleNames) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }

        String otp = GenerateRandomOTP.generateOTP(6);
        // Gửi mail OTP qua RabbitMQ
        MailRequestDto mailRequest = new MailRequestDto(
                savedUser.getEmail(),
                "Welcome to eMartix: Verify your email",
                "Your OTP code is: " + otp
        );
        mailProducer.sendMail(mailRequest);
        tokenService.saveOtp(user.getEmail(), otp, TimeUnit.MINUTES.toSeconds(10)); // Lưu OTP sống 10 phút
        return userService.getUserDetails(savedUser.getUsername());
    }

    @Override
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // Xóa thông tin xác thực khỏi SecurityContext
            SecurityContextHolder.getContext().setAuthentication(null);
            // Xóa token khỏi Redis
            String username = authentication.getName();
            tokenService.deleteToken(username);
            log.info("User {} logged out successfully", username);
        }
        // Xóa JWT cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Kiểm tra xem có cookie nào tên là "refreshToken"
                if ("refreshToken".equals(cookie.getName())) {
                    // Trả về giá trị của refreshToken
                    cookie = new Cookie("refreshToken", null);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true); // Chỉ set secure nếu bạn sử dụng https
                    cookie.setPath("/"); // Đảm bảo cookie được xóa ở toàn bộ ứng dụng
                    cookie.setMaxAge(0); // Đặt lại giá trị age thành 0 để xóa cookie
                    response.addCookie(cookie);
                }
            }
        }
        return "Logout successful";
    }

    @Override
    public boolean verifyEmail(VerifyOtpRequestDto requestDto) {
        // exits user
        User user = userRepository.findByUsernameOrEmail(requestDto.getUsername(), requestDto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username or email",requestDto.getUsername()));
        String storedOtp = tokenService.getOtp(user.getEmail());
        // check otp
        if (!storedOtp.equals(requestDto.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }else{
            tokenService.deleteOtp(user.getEmail());
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }
        return true;
    }

    @Override
    public void sentRequireForgotPassword(ForgotPasswordRequestDto requestDto) {
        if (requestDto.getNewPassword() == null || requestDto.getConfirmPassword() == null || !requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
        String link = generateLink(requestDto.getUsernameOrEmail(), requestDto.getNewPassword());

        // Gửi mail với link xác thực
        MailRequestDto mailRequest = new MailRequestDto(
                requestDto.getUsernameOrEmail(),
                "Confirm your password reset request",
                "Click this link to conform " + link);
        mailProducer.sendMail(mailRequest);
        log.info("Reset request password for account::: {}", requestDto.getUsernameOrEmail());
    }

    @Override
    public void resentOtp(ResentOtpRequestDto requestDto) {
        // exits user
        User user = userRepository.findByUsernameOrEmail(requestDto.getUsernameOrEmail(), requestDto.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "usename or email", requestDto.getUsernameOrEmail()));

        String otp = GenerateRandomOTP.generateOTP(6);
        // Gửi mail OTP qua RabbitMQ
        MailRequestDto mailRequest = new MailRequestDto(
                user.getEmail(),
                "Welcome to eMartix: Verify your email",
                "Your OTP code is: " + otp
        );
        mailProducer.sendMail(mailRequest);
        tokenService.saveOtp(user.getEmail(), otp, TimeUnit.MINUTES.toSeconds(10)); // Lưu OTP sống 10 phút
    }

    @Override
    public void verifyLink(String username,String token, String password) {
        User user = userRepository.findByUsernameOrEmail(username, username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username || email", username)
        );
        if( user != null && token != null && tokenService.getTokenResetPassword(username).equals(token) ){
            // Xóa token khỏi Redis
            tokenService.deleteKey("resetPassword:"+username);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }
    }

    private String generateLink(String username, String password) {
        String token = UUID.randomUUID().toString();
        tokenService.saveTokenResetPassword(username, token);
        // Tạo link xác thực
        return String.format("http://localhost:8080/api/v1/auth/verify-link?username=%s&token=%s&password=%s", username, token,password);
    }
}
