package com.lendflow.api.service;

import com.lendflow.api.entity.Loan;
import com.lendflow.api.entity.User;
import com.lendflow.api.exception.UserNotFoundException;
import com.lendflow.api.repository.LoanRepository;
import com.lendflow.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CreditAssessmentService creditAssessmentService;

    @InjectMocks
    private LoanService loanService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("borrower@example.com");
        user.setMonthlyIncome(new BigDecimal("3000000"));
        user.setEmploymentMonths(24);
    }

    @Test
    void applyForLoan_withHighScore_isAutoApproved() {
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(user));
        when(creditAssessmentService.isAmountWithinLimit(any(), any())).thenReturn(true);
        when(creditAssessmentService.calculateScore(user)).thenReturn(85);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.applyForLoan("borrower@example.com", new BigDecimal("1200000"), 12);

        assertThat(loan.getStatus()).isEqualTo(Loan.LoanStatus.APPROVED);
        assertThat(loan.getCreditScore()).isEqualTo(85);
        assertThat(loan.getInterestRate()).isEqualByComparingTo("18.00");
        assertThat(loan.getUser()).isSameAs(user);
    }

    @Test
    void applyForLoan_withLowScore_goesToUnderReview() {
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(user));
        when(creditAssessmentService.isAmountWithinLimit(any(), any())).thenReturn(true);
        when(creditAssessmentService.calculateScore(user)).thenReturn(65);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.applyForLoan("borrower@example.com", new BigDecimal("1200000"), 12);

        assertThat(loan.getStatus()).isEqualTo(Loan.LoanStatus.UNDER_REVIEW);
    }

    @Test
    void applyForLoan_withNegativeAmount_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                loanService.applyForLoan("borrower@example.com", new BigDecimal("-100"), 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void applyForLoan_withUnknownUser_throwsUserNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loanService.applyForLoan("ghost@example.com", new BigDecimal("1200000"), 12))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void applyForLoan_amountOverTenTimesIncome_throwsIllegalArgument() {
        when(userRepository.findByEmail("borrower@example.com")).thenReturn(Optional.of(user));
        when(creditAssessmentService.isAmountWithinLimit(any(), any())).thenReturn(false);

        assertThatThrownBy(() ->
                loanService.applyForLoan("borrower@example.com", new BigDecimal("999000000"), 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
    }
}
