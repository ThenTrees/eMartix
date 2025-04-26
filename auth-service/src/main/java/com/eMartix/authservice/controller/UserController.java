package com.eMartix.authservice.controller;


import com.eMartix.authservice.dto.request.ChangePasswordRequestDto;
import com.eMartix.authservice.dto.request.UpdateProfileRequestDto;
import com.eMartix.authservice.dto.response.ListUserResponse;
import com.eMartix.authservice.dto.response.UserResponseDto;
import com.eMartix.authservice.model.Permission;
import com.eMartix.authservice.model.Role;
import com.eMartix.authservice.model.User;
import com.eMartix.authservice.service.RoleService;
import com.eMartix.authservice.service.UserService;
import com.eMartix.commons.dtos.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ListUserResponse>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.<ListUserResponse>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Get all users successfully")
                .response(ListUserResponse.builder()
                    .users(userService.getAllUsers())
                    .build())
                .build());
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<UserResponseDto>builder()
                            .code(HttpStatus.UNAUTHORIZED.value())
                            .success(false)
                            .message("Unauthorized")
                            .response(null)
                            .build()
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<UserResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .success(true)
                        .message("Get current user successfully")
                        .response(userService.getUserDetails(authentication.getName()))
                        .build()
        );
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.builder()
                        .success(true)
                        .message("User deleted successfully")
                        .response(null)
                        .code(HttpStatus.NO_CONTENT.value())
                        .build()
        );
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        return ResponseEntity.ok(
                ApiResponse.<List<Role>>builder()
                        .success(true)
                        .code(HttpStatus.OK.value())
                        .message("Get all roles successfully")
                        .response(roleService.getAllRoles())
                        .build()
        );
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.ok(
                ApiResponse.<Role>builder()
                        .success(true)
                        .code(HttpStatus.CREATED.value())
                        .message("Role created successfully")
                        .response(createdRole)
                        .build()
        );
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        return ResponseEntity.ok(
                ApiResponse.<List<Permission>>builder()
                        .success(true)
                        .code(HttpStatus.OK.value())
                        .message("Get all permissions successfully")
                        .response(roleService.getAllPermissions())
                        .build()
        );
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Permission>> createPermission(@RequestBody Permission permission) {
        Permission createdPermission = roleService.createPermission(permission);
        return ResponseEntity.ok(
                ApiResponse.<Permission>builder()
                        .success(true)
                        .code(HttpStatus.CREATED.value())
                        .message("Permission created successfully")
                        .response(createdPermission)
                        .build()
        );
    }

    @GetMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Permission>>> getRolePermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(
                ApiResponse.<List<Permission>>builder()
                        .success(true)
                        .code(HttpStatus.OK.value())
                        .message("Get role permissions successfully")
                        .response(roleService.getRolePermissions(roleId))
                        .build()
        );
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Permission added to role successfully")
                        .response(null)
                        .code(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Permission removed from role successfully")
                        .response(null)
                        .code(HttpStatus.OK.value())
                        .build()
        );
    }

    @PutMapping("/users/change-password")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRoles(
            @RequestBody ChangePasswordRequestDto changePasswordRequestDto,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();
        userService.changePassword(username, changePasswordRequestDto);
        return ResponseEntity.ok(
                ApiResponse.<UserResponseDto>builder()
                        .success(true)
                        .code(HttpStatus.OK.value())
                        .message("Password changed successfully")
                        .response(userService.getUserDetails(username))
                        .build()
        );
    }

    @PutMapping("/users")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @RequestBody UpdateProfileRequestDto updateProfileRequestDto,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        userService.updateProfile(userId, updateProfileRequestDto);
        return ResponseEntity.ok(
                ApiResponse.<UserResponseDto>builder()
                        .success(true)
                        .code(HttpStatus.OK.value())
                        .message("Profile updated successfully")
                        .response(userService.getUserDetails(user.getUsername()))
                        .build()
        );
    }
}