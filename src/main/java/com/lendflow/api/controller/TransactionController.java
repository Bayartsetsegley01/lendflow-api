// src/main/java/com/lendflow/api/controller/TransactionController.java
package com.lendflow.api.controller;

import com.lendflow.api.entity.Transaction;
import com.lendflow.api.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // GET /api/transactions — зөвхөн нэвтэрсэн хэрэглэгчийн өөрийн гүйлгээний түүх.
    // Хэрэглэгчийн имэйлийг JWT token-оос ирсэн Authentication-аас авна.
    @GetMapping
    public ResponseEntity<List<Transaction>> getMyTransactions(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionService.getMyTransactions(userEmail));
    }
}
