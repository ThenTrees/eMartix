package com.eMartix.authservice.dto.request;

import com.eMartix.authservice.common.UserType;
import com.eMartix.authservice.util.ValidUserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @NotEmpty(message = "full name should not be empty")
    private String fullName;

    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Phone number should be valid - phone must be Vietnamese phone number")
    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    @NotEmpty(message = "Username should not be empty")
    private String username;

    @NotEmpty(message = "Password should not be empty")
    private String password;

    @ValidUserType
    private UserType type;

    private List<String> roles;
}
