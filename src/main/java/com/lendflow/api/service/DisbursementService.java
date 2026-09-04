// src/main/java/com/lendflow/api/service/DisbursementService.java
package com.lendflow.api.service;

import com.lendflow.api.entity.Account;
import com.lendflow.api.entity.Loan;
import com.lendflow.api.entity.Transaction;
import com.lendflow.api.repository.AccountRepository;
import com.lendflow.api.repository.LoanRepository;
import com.lendflow.api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisbursementService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RepaymentService repaymentService;

    public DisbursementService(LoanRepository loanRepository, AccountRepository accountRepository,
                                TransactionRepository transactionRepository, RepaymentService repaymentService) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.repaymentService = repaymentService;
    }

    @Transactional
    public Loan disburse(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Only APPROVED loans can be disbursed");
        }

        Account account = accountRepository.findByUserId(loan.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Account not found for user"));

        account.setBalance(account.getBalance().add(loan.getAmount()));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setUser(loan.getUser());
        transaction.setLoan(loan);
        transaction.setType(Transaction.TransactionType.DISBURSEMENT);
        transaction.setAmount(loan.getAmount());
        transactionRepository.save(transaction);

        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loanRepository.save(loan);

        // Мөнгө олгогдмогц, сар бүрийн төлөлтийн хуваарийг автоматаар үүсгэнэ
        repaymentService.generateSchedule(loan);

        return loan;
    }
}