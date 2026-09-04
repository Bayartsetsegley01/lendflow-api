// src/main/java/com/lendflow/api/entity/Transaction.java
package com.lendflow.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Аль хэрэглэгчийн үйлдэл болохыг заана
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Аль зээлтэй холбоотой болохыг заана (заавал биш — ирээдүйд зээлгүй
    // үйлдэл гарвал ашиглаж болно, тиймээс nullable = true)
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = true)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.COMPLETED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TransactionStatus.COMPLETED;
        }
    }

    public enum TransactionType {
        DISBURSEMENT,  // Зээл олгогдоход — Account руу мөнгө орно
        REPAYMENT      // Хэрэглэгч төлөлт хийхэд — Account-аас мөнгө гарна
    }

    public enum TransactionStatus {
        COMPLETED,
        FAILED
    }
}