// src/main/java/com/lendflow/api/exception/GlobalExceptionHandler.java
package com.lendflow.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Бүх Controller-оос гарсан алдааг нэг дор барьж, жигд бүтэцтэй JSON болгож буцаана.
// Ингэснээр алдаа Spring-ийн /error dispatch руу хүрэхээс өмнө энд боловсруулагдана.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // {"timestamp":..., "status":..., "error":..., "message":...} үндсэн бүтэц
    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    // --- 404 Not Found ---
    @ExceptionHandler({
            LoanNotFoundException.class,
            AccountNotFoundException.class,
            RepaymentNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(baseBody(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    // --- 400 Bad Request (бизнес дүрэм зөрчсөн) ---
    @ExceptionHandler({
            InsufficientBalanceException.class,
            RepaymentAlreadyPaidException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(baseBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // --- 400 Bad Request (@Valid DTO validation) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // --- 500 Internal Server Error (бусад бүх RuntimeException) ---
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
