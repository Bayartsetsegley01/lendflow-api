// src/main/java/com/lendflow/api/service/LoanService.java
package com.lendflow.api.service;

import com.lendflow.api.entity.Loan;
import com.lendflow.api.entity.User;
import com.lendflow.api.exception.LoanNotFoundException;
import com.lendflow.api.exception.UserNotFoundException;
import com.lendflow.api.repository.LoanRepository;
import com.lendflow.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CreditAssessmentService creditAssessmentService;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository,
                        CreditAssessmentService creditAssessmentService) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.creditAssessmentService = creditAssessmentService;
    }

    // Зээлийн хүсэлт үүсгэж, шууд Credit Assessment хийж UNDER_REVIEW болгоно
    public Loan applyForLoan(String userEmail, BigDecimal amount, Integer termMonths) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be positive");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term months must be positive");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        // Зээлийн дүн сарын орлогод харьцангуй хэт их бол шууд татгалзана
        if (!creditAssessmentService.isAmountWithinLimit(user, amount)) {
            throw new IllegalArgumentException(
                    "Requested amount exceeds allowed limit based on your monthly income");
        }

        int score = creditAssessmentService.calculateScore(user);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAmount(amount);
        loan.setTermMonths(termMonths);
        loan.setInterestRate(new BigDecimal("18.00"));
        loan.setCreditScore(score);

        // Score >= 70 бол шууд APPROVED, эс бол Admin шалгах UNDER_REVIEW төлөвт үлдэнэ
        if (score >= 70) {
            loan.setStatus(Loan.LoanStatus.APPROVED);
        } else {
            loan.setStatus(Loan.LoanStatus.UNDER_REVIEW);
        }

        return loanRepository.save(loan);
    }

    public List<Loan> getMyLoans(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
        return loanRepository.findByUserId(user.getId());
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));
    }

    // Admin-д зориулсан методууд
    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    public Loan approveLoan(Long id) {
        Loan loan = getLoanById(id);
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new IllegalArgumentException("Only UNDER_REVIEW loans can be approved");
        }
        loan.setStatus(Loan.LoanStatus.APPROVED);
        return loanRepository.save(loan);
    }

    public Loan rejectLoan(Long id) {
        Loan loan = getLoanById(id);
        if (loan.getStatus() != Loan.LoanStatus.UNDER_REVIEW) {
            throw new IllegalArgumentException("Only UNDER_REVIEW loans can be rejected");
        }
        loan.setStatus(Loan.LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }
}