package com.ongmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Set;

@Getter @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private Boolean enabled;
    private Set<String> roles;
}
