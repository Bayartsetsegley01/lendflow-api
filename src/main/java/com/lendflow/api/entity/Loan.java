// src/main/java/com/lendflow/api/entity/Loan.java
package com.lendflow.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Олон Loan нэг User-т харьяалагдана (нэг хэрэглэгч олон зээл авч болно)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Жилийн хүүгийн хувь, жишээ нь 18.00 гэдэг нь 18%
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    // CreditAssessmentService тооцож өгдөг оноо (0-100)
    @Column(name = "credit_score")
    private Integer creditScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LoanStatus.PENDING;
        }
    }

    // Зээлийн явцын төлөв — эхнээс дуустал шаталсан дарааллаар шилжинэ
    public enum LoanStatus {
        PENDING,        // Хэрэглэгч хүсэлт илгээсэн, хараахан шалгагдаагүй
        UNDER_REVIEW,   // Credit assessment хийгдэж байгаа
        APPROVED,       // Зөвшөөрөгдсөн, мөнгө хараахан олгогдоогүй
        REJECTED,       // Татгалзсан
        DISBURSED,      // Мөнгө олгогдсон (Account руу орсон)
        ACTIVE,         // Идэвхтэй, төлөлт хийгдэж байгаа
        PAID_OFF        // Бүрэн төлөгдсөн
    }
}