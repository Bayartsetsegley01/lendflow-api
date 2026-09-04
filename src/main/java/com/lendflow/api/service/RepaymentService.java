// src/main/java/com/lendflow/api/service/RepaymentService.java
package com.lendflow.api.service;

import com.lendflow.api.entity.Account;
import com.lendflow.api.entity.Loan;
import com.lendflow.api.entity.Repayment;
import com.lendflow.api.entity.Transaction;
import com.lendflow.api.repository.AccountRepository;
import com.lendflow.api.repository.RepaymentRepository;
import com.lendflow.api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmiCalculator emiCalculator;

    public RepaymentService(RepaymentRepository repaymentRepository, AccountRepository accountRepository,
                             TransactionRepository transactionRepository, EmiCalculator emiCalculator) {
        this.repaymentRepository = repaymentRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.emiCalculator = emiCalculator;
    }

    public void generateSchedule(Loan loan) {
        BigDecimal monthlyPayment = emiCalculator.calculateMonthlyPayment(
                loan.getAmount(), loan.getInterestRate(), loan.getTermMonths()
        );

        LocalDate firstDueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        for (int i = 0; i < loan.getTermMonths(); i++) {
            Repayment repayment = new Repayment();
            repayment.setLoan(loan);
            repayment.setDueDate(firstDueDate.plusMonths(i));
            repayment.setAmount(monthlyPayment);
            repayment.setPaidAmount(BigDecimal.ZERO);
            repayment.setStatus(Repayment.RepaymentStatus.UPCOMING);
            repaymentRepository.save(repayment);
        }
    }

    public List<Repayment> getScheduleByLoanId(Long loanId) {
        return repaymentRepository.findByLoanIdOrderByDueDateAsc(loanId);
    }

    @Transactional
    public Repayment pay(Long repaymentId) {
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RuntimeException("Repayment not found with id: " + repaymentId));

        if (repayment.getStatus() == Repayment.RepaymentStatus.PAID) {
            throw new IllegalArgumentException("This repayment has already been paid");
        }

        Loan loan = repayment.getLoan();
        Account account = accountRepository.findByUserId(loan.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(repayment.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(repayment.getAmount()));
        accountRepository.save(account);

        repayment.setPaidAmount(repayment.getAmount());
        repayment.setStatus(Repayment.RepaymentStatus.PAID);
        repayment.setPaidAt(java.time.LocalDateTime.now());
        repaymentRepository.save(repayment);

        Transaction transaction = new Transaction();
        transaction.setUser(loan.getUser());
        transaction.setLoan(loan);
        transaction.setType(Transaction.TransactionType.REPAYMENT);
        transaction.setAmount(repayment.getAmount());
        transactionRepository.save(transaction);

        return repayment;
    }
}