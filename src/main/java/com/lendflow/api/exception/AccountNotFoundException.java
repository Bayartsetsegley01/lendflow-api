// src/main/java/com/lendflow/api/exception/AccountNotFoundException.java
package com.lendflow.api.exception;

// Хэрэглэгчийн данс олдоогүй үед шидэгдэнэ → HTTP 404
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
