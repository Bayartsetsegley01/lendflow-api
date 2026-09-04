// src/main/java/com/lendflow/api/exception/RepaymentNotFoundException.java
package com.lendflow.api.exception;

// Төлөлтийн мөр олдоогүй үед шидэгдэнэ → HTTP 404
public class RepaymentNotFoundException extends RuntimeException {
    public RepaymentNotFoundException(String message) {
        super(message);
    }
}
