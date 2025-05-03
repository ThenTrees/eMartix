package com.eMartix.authservice.dto.response;

import com.eMartix.authservice.common.UserStatus;
import com.eMartix.authservice.common.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserResponseDto {

    private String fullName;

    private String phone;

    private String email;

    private String username;

    private UserType type;

    private UserStatus status;

    private List<String> roles;

    @JsonIgnore
    private List<String> permissions;

}
