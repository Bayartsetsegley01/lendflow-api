// src/main/java/com/lendflow/api/repository/LoanRepository.java
package com.lendflow.api.repository;

import com.lendflow.api.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Хэрэглэгч өөрийн бүх зээлээ харах endpoint-д ашиглана
    List<Loan> findByUserId(Long userId);

    // Admin-ийн "Loan Applications" жагсаалтад ашиглана (жишээ нь зөвхөн UNDER_REVIEW-г харах)
    List<Loan> findByStatus(Loan.LoanStatus status);
}