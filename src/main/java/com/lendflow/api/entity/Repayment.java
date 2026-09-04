// src/main/java/com/lendflow/api/entity/Repayment.java
package com.lendflow.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "repayments")
@Getter
@Setter
@NoArgsConstructor
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Олон Repayment нэг Loan-д харьяалагдана (12 сарын зээл бол 12 Repayment мөр байна)
    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    // Энэ төлөлтийг хийх ёстой огноо (жишээ нь сар бүрийн 1-ний өдөр)
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Төлөх ёстой нийт дүн (EMI тооцоолсон дүн)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Хэрэглэгч бодитоор төлсөн дүн (эхлээд 0, төлөхөд amount-тай тэнцүү болно)
    @Column(name = "paid_amount", precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatus status = RepaymentStatus.UPCOMING;

    // Хэзээ бодитоор төлөгдсөнийг тэмдэглэнэ (төлөгдөөгүй бол null)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public enum RepaymentStatus {
        UPCOMING,  // Хараахан хугацаа болоогүй
        PAID,      // Төлөгдсөн
        OVERDUE    // Хугацаа хэтэрсэн ч төлөгдөөгүй
    }
}