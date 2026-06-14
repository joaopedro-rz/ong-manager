package com.ongmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "OK", OffsetDateTime.now().toString());
    }
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, OffsetDateTime.now().toString());
    }
}
