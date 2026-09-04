// src/main/java/com/lendflow/api/exception/RepaymentAlreadyPaidException.java
package com.lendflow.api.exception;

// Аль хэдийн төлөгдсөн төлөлтийг дахин төлөх гэж оролдоход шидэгдэнэ → HTTP 400
public class RepaymentAlreadyPaidException extends RuntimeException {
    public RepaymentAlreadyPaidException(String message) {
        super(message);
    }
}
