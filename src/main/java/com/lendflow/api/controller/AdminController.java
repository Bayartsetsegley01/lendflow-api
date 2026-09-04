// src/main/java/com/lendflow/api/controller/AdminController.java
package com.lendflow.api.controller;

import com.lendflow.api.entity.Loan;
import com.lendflow.api.service.DisbursementService;
import com.lendflow.api.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LoanService loanService;
    private final DisbursementService disbursementService;

    public AdminController(LoanService loanService, DisbursementService disbursementService) {
        this.loanService = loanService;
        this.disbursementService = disbursementService;
    }

    // Admin-ийн шалгах ёстой зээлийн жагсаалт
    @GetMapping("/loans")
    public ResponseEntity<List<Loan>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getLoansByStatus(Loan.LoanStatus.UNDER_REVIEW));
    }

    @PatchMapping("/loans/{id}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    @PatchMapping("/loans/{id}/reject")
    public ResponseEntity<Loan> rejectLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.rejectLoan(id));
    }
}