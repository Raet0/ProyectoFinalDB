package com.example.potcast_back.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Error procesando la solicitud: " + e.getMessage());
        error.put("type", e.getClass().getSimpleName());
        
        e.printStackTrace();
        System.err.println("ERROR GLOBAL: " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Error: Valor nulo inesperado - " + e.getMessage());
        error.put("type", "NullPointerException");
        
        e.printStackTrace();
        System.err.println("NULL POINTER ERROR: " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Error de argumento inválido: " + e.getMessage());
        error.put("type", "IllegalArgumentException");
        
        e.printStackTrace();
        System.err.println("ILLEGAL ARGUMENT ERROR: " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
