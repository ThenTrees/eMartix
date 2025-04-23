package com.eMartix.authservice.mapper;

import com.eMartix.authservice.dto.response.UserResponseDto;
import com.eMartix.authservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Để Spring quản lý Mapper như 1 bean
public interface UserMapper {
    // Chuyển đổi từ User entity sang UserResponseDto
    UserResponseDto toUserResponseDto(User user);

    // Chuyển đổi từ UserResponseDto sang User entity
    User toUserEntity(UserResponseDto userResponseDto);
}