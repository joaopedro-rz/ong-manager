package com.ongmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> body(ErrorCode code, String message, List<?> details, String path) {
        Map<String, Object> err = new HashMap<>();
        err.put("code", code.name());
        err.put("message", message);
        err.put("details", details == null ? List.of() : details);
        err.put("timestamp", OffsetDateTime.now().toString());
        err.put("path", path);
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("success", false);
        wrapper.put("error", err);
        return wrapper;
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleApp(AppException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
            .body(body(ex.getCode(), ex.getMessage(), null, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String,String>> details = ex.getBindingResult().getFieldErrors().stream()
            .map((FieldError f) -> Map.of("field", f.getField(), "message",
                f.getDefaultMessage() == null ? "invalid" : f.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(
            body(ErrorCode.VALIDATION_ERROR, "Dados invalidos", details, req.getRequestURI()));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<?> handleBadCreds(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(401).body(
            body(ErrorCode.INVALID_CREDENTIALS, "Credenciais invalidas", null, req.getRequestURI()));
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<?> handleAccountStatus(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(403).body(
            body(ErrorCode.EMAIL_NOT_VERIFIED, "Conta indisponivel para login", null, req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(403).body(
            body(ErrorCode.INSUFFICIENT_PERMISSIONS, "Acesso negado", null, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erro nao tratado", ex);
        return ResponseEntity.internalServerError().body(
            body(ErrorCode.INTERNAL_ERROR, "Erro interno do servidor", null, req.getRequestURI()));
    }
}
