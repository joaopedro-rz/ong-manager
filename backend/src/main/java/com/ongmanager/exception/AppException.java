package com.ongmanager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode code;
    private final HttpStatus status;

    public AppException(ErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static AppException notFound(String msg) {
        return new AppException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, msg);
    }
    public static AppException badRequest(ErrorCode code, String msg) {
        return new AppException(code, HttpStatus.BAD_REQUEST, msg);
    }
    public static AppException unauthorized(String msg) {
        return new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, msg);
    }
    public static AppException forbidden(String msg) {
        return new AppException(ErrorCode.INSUFFICIENT_PERMISSIONS, HttpStatus.FORBIDDEN, msg);
    }
    public static AppException conflict(String msg) {
        return new AppException(ErrorCode.DUPLICATE_ENTRY, HttpStatus.CONFLICT, msg);
    }
}
