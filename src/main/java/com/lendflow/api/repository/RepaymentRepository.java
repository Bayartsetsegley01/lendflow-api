// src/main/java/com/lendflow/api/repository/RepaymentRepository.java
package com.lendflow.api.repository;

import com.lendflow.api.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    // Тухайн зээлийн бүх сарын төлөлтийн хуваарийг харах endpoint-д ашиглана
    List<Repayment> findByLoanIdOrderByDueDateAsc(Long loanId);
}