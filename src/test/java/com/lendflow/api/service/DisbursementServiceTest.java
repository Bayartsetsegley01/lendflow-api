package com.lendflow.api.service;

import com.lendflow.api.entity.Account;
import com.lendflow.api.entity.Loan;
import com.lendflow.api.entity.Transaction;
import com.lendflow.api.entity.User;
import com.lendflow.api.repository.AccountRepository;
import com.lendflow.api.repository.LoanRepository;
import com.lendflow.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisbursementServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RepaymentService repaymentService;

    @InjectMocks
    private DisbursementService disbursementService;

    private User user;
    private Loan loan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        loan = new Loan();
        loan.setId(10L);
        loan.setUser(user);
        loan.setAmount(new BigDecimal("1000000"));
        loan.setTermMonths(12);
        loan.setInterestRate(new BigDecimal("18.00"));
    }

    @Test
    void disburse_approvedLoan_activatesLoanCreditsAccountAndGeneratesSchedule() {
        loan.setStatus(Loan.LoanStatus.APPROVED);
        Account acc = new Account();
        acc.setUser(user);
        acc.setBalance(new BigDecimal("200000"));

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(acc));

        Loan result = disbursementService.disburse(10L);

        assertThat(result.getStatus()).isEqualTo(Loan.LoanStatus.ACTIVE);
        assertThat(acc.getBalance()).isEqualByComparingTo("1200000"); // 200000 + 1000000

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo(Transaction.TransactionType.DISBURSEMENT);
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("1000000");

        // Мөнгө олгогдмогц төлөлтийн хуваарь автоматаар үүсэх ёстой
        verify(repaymentService).generateSchedule(loan);
    }

    @Test
    void disburse_nonApprovedLoan_throwsAndDoesNothing() {
        loan.setStatus(Loan.LoanStatus.PENDING);
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> disbursementService.disburse(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("APPROVED");

        assertThat(loan.getStatus()).isEqualTo(Loan.LoanStatus.PENDING);
        verify(transactionRepository, never()).save(any());
        verify(repaymentService, never()).generateSchedule(any());
    }
}
