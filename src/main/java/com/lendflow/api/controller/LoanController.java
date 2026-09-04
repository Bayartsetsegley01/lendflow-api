// src/main/java/com/lendflow/api/controller/LoanController.java
package com.lendflow.api.controller;

import com.lendflow.api.entity.Loan;
import com.lendflow.api.service.DisbursementService;
import com.lendflow.api.service.LoanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final DisbursementService disbursementService;

    public LoanController(LoanService loanService, DisbursementService disbursementService) {
        this.loanService = loanService;
        this.disbursementService = disbursementService;
    }

    // POST /api/loans — Authentication параметр нь Spring Security-ийн JwtAuthFilter
    // дотор бид SecurityContext-д тавьсан хэрэглэгчийн имэйлийг автоматаар дамжуулна.
    // Ингэснээр client "би хэн бэ" гэдгээ дамжуулах шаардлагагүй болно — token дотроос уншина.
    @PostMapping
    public ResponseEntity<Loan> applyForLoan(
            @Valid @RequestBody LoanRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Loan loan = loanService.applyForLoan(userEmail, request.amount(), request.termMonths());
        return ResponseEntity.status(201).body(loan);
    }

    // GET /api/loans — зөвхөн НЭВТЭРСЭН хэрэглэгчийн ӨӨРИЙНХ зээлийг л харуулна
    @GetMapping
    public ResponseEntity<List<Loan>> getMyLoans(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(loanService.getMyLoans(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    // POST /api/loans/{id}/disburse — APPROVED зээлд мөнгө олгож, Account balance нэмнэ
    @PostMapping("/{id}/disburse")
    public ResponseEntity<Loan> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(disbursementService.disburse(id));
    }

    public record LoanRequest(
            @NotNull(message = "amount is required")
            @Positive(message = "amount must be positive")
            BigDecimal amount,

            @NotNull(message = "termMonths is required")
            @Positive(message = "termMonths must be positive")
            Integer termMonths
    ) {}
}