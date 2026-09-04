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

    public DisbursementService(LoanRepository loanRepository, AccountRepository accountRepository,
                                TransactionRepository transactionRepository) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // @Transactional чухал шалтгаан: Loan status, Account balance, Transaction record
    // 3 нь бүгд амжилттай эсвэл бүгд rollback байх ёстой
    @Transactional
    public Loan disburse(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Only APPROVED loans can be disbursed");
        }

        Account account = accountRepository.findByUserId(loan.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Account not found for user"));

        // 1. Дансны үлдэгдэлд зээлийн мөнгийг нэмнэ
        account.setBalance(account.getBalance().add(loan.getAmount()));
        accountRepository.save(account);

        // 2. Зээлийн төлвийг шинэчилнэ
        loan.setStatus(Loan.LoanStatus.DISBURSED);
        loanRepository.save(loan);

        // 3. Гүйлгээний бүртгэл үүсгэнэ
        Transaction transaction = new Transaction();
        transaction.setUser(loan.getUser());
        transaction.setLoan(loan);
        transaction.setType(Transaction.TransactionType.DISBURSEMENT);
        transaction.setAmount(loan.getAmount());
        transactionRepository.save(transaction);

        // Мөнгө олгогдсоны дараа зээл ACTIVE (идэвхтэй, төлөгдөж эхлэх ёстой) болно
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        return loanRepository.save(loan);
    }
}