// src/main/java/com/lendflow/api/controller/RepaymentController.java
package com.lendflow.api.controller;

import com.lendflow.api.entity.Repayment;
import com.lendflow.api.service.RepaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    // GET /api/loans/{loanId}/repayments — сар бүрийн хуваарь харах
    @GetMapping("/loans/{loanId}/repayments")
    public ResponseEntity<List<Repayment>> getSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(repaymentService.getScheduleByLoanId(loanId));
    }

    // POST /api/repayments/{id}/pay — тухайн сарын төлбөрийг төлөх
    @PostMapping("/repayments/{id}/pay")
    public ResponseEntity<Repayment> pay(@PathVariable Long id) {
        return ResponseEntity.ok(repaymentService.pay(id));
    }
}