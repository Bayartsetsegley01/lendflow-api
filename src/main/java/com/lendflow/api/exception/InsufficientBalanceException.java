// src/main/java/com/lendflow/api/exception/InsufficientBalanceException.java
package com.lendflow.api.exception;

// Данс дахь үлдэгдэл төлбөрт хүрэлцэхгүй үед шидэгдэнэ → HTTP 400
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
