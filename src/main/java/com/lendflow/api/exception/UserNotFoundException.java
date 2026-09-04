// src/main/java/com/lendflow/api/exception/UserNotFoundException.java
package com.lendflow.api.exception;

// Хэрэглэгч олдоогүй үед шидэгдэнэ → HTTP 404
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
