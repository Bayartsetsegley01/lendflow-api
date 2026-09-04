package com.lendflow.api.service;

import com.lendflow.api.entity.Account;
import com.lendflow.api.entity.Loan;
import com.lendflow.api.entity.Repayment;
import com.lendflow.api.entity.Transaction;
import com.lendflow.api.entity.User;
import com.lendflow.api.exception.InsufficientBalanceException;
import com.lendflow.api.exception.RepaymentAlreadyPaidException;
import com.lendflow.api.repository.AccountRepository;
import com.lendflow.api.repository.RepaymentRepository;
import com.lendflow.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepaymentServiceTest {

    @Mock
    private RepaymentRepository repaymentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EmiCalculator emiCalculator;

    @InjectMocks
    private RepaymentService repaymentService;

    private User user;
    private Loan loan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("borrower@example.com");

        loan = new Loan();
        loan.setId(10L);
        loan.setUser(user);
        loan.setAmount(new BigDecimal("1200000"));
        loan.setInterestRate(new BigDecimal("18.00"));
        loan.setTermMonths(12);
    }

    private Repayment repayment(Repayment.RepaymentStatus status, String amount) {
        Repayment r = new Repayment();
        r.setId(100L);
        r.setLoan(loan);
        r.setAmount(new BigDecimal(amount));
        r.setPaidAmount(BigDecimal.ZERO);
        r.setDueDate(LocalDate.now().plusMonths(1));
        r.setStatus(status);
        return r;
    }

    private Account account(String balance) {
        Account a = new Account();
        a.setId(5L);
        a.setUser(user);
        a.setBalance(new BigDecimal(balance));
        return a;
    }

    @Test
    void pay_withSufficientBalance_succeedsAndDeductsBalance() {
        Repayment r = repayment(Repayment.RepaymentStatus.UPCOMING, "110015.99");
        Account acc = account("500000");
        when(repaymentRepository.findById(100L)).thenReturn(Optional.of(r));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(acc));

        Repayment result = repaymentService.pay(100L);

        assertThat(result.getStatus()).isEqualTo(Repayment.RepaymentStatus.PAID);
        assertThat(result.getPaidAmount()).isEqualByComparingTo("110015.99");
        assertThat(result.getPaidAt()).isNotNull();
        assertThat(acc.getBalance()).isEqualByComparingTo("389984.01"); // 500000 - 110015.99

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getType()).isEqualTo(Transaction.TransactionType.REPAYMENT);
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("110015.99");
    }

    @Test
    void pay_withInsufficientBalance_throwsAndDoesNotChangeState() {
        Repayment r = repayment(Repayment.RepaymentStatus.UPCOMING, "110015.99");
        Account acc = account("50000");
        when(repaymentRepository.findById(100L)).thenReturn(Optional.of(r));
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> repaymentService.pay(100L))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(r.getStatus()).isEqualTo(Repayment.RepaymentStatus.UPCOMING);
        assertThat(acc.getBalance()).isEqualByComparingTo("50000");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void pay_alreadyPaidRepayment_throwsRepaymentAlreadyPaid() {
        Repayment r = repayment(Repayment.RepaymentStatus.PAID, "110015.99");
        when(repaymentRepository.findById(100L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> repaymentService.pay(100L))
                .isInstanceOf(RepaymentAlreadyPaidException.class)
                .hasMessageContaining("already been paid");

        verify(accountRepository, never()).findByUserId(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void generateSchedule_createsExactlyTermMonthsRepayments_withConsecutiveDueDates() {
        when(emiCalculator.calculateMonthlyPayment(any(), any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(new BigDecimal("110015.99"));

        repaymentService.generateSchedule(loan);

        ArgumentCaptor<Repayment> captor = ArgumentCaptor.forClass(Repayment.class);
        verify(repaymentRepository, times(12)).save(captor.capture());

        List<Repayment> saved = captor.getAllValues();
        assertThat(saved).hasSize(12);

        LocalDate expectedFirst = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        for (int i = 0; i < 12; i++) {
            Repayment r = saved.get(i);
            assertThat(r.getLoan()).isSameAs(loan);
            assertThat(r.getAmount()).isEqualByComparingTo("110015.99");
            assertThat(r.getPaidAmount()).isEqualByComparingTo("0");
            assertThat(r.getStatus()).isEqualTo(Repayment.RepaymentStatus.UPCOMING);
            assertThat(r.getDueDate()).isEqualTo(expectedFirst.plusMonths(i));
        }
    }
}
