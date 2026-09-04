// src/main/java/com/lendflow/api/exception/LoanNotFoundException.java
package com.lendflow.api.exception;

// Зээл олдоогүй үед шидэгдэнэ → HTTP 404
public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(String message) {
        super(message);
    }
}
