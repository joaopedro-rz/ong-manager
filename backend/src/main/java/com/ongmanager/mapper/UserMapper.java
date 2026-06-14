package com.ongmanager.mapper;

import com.ongmanager.dto.response.UserResponse;
import com.ongmanager.entity.Role;
import com.ongmanager.entity.User;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponse(User u) {
        return new UserResponse(
            u.getId(), u.getName(), u.getEmail(), u.getPhone(),
            u.getProfileImageUrl(), u.getEnabled(),
            u.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
