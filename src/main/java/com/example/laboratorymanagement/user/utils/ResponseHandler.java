package com.example.laboratorymanagement.user.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {

    public static ResponseEntity<?> success() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(data);
    }

    public static <T> ResponseEntity<T> badRequest(T data) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(data);
    }

    public static <T> ResponseEntity<T> unauthorized() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    public static <T> ResponseEntity<T> unauthorized(T data) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(data);
    }

    public static ResponseEntity<?> notFound() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> ResponseEntity<T> notFound(T data) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(data);
    }
}
