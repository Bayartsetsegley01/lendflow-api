// src/main/java/com/lendflow/api/repository/TransactionRepository.java
package com.lendflow.api.repository;

import com.lendflow.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Хэрэглэгчийн бүх гүйлгээний түүхийг харах endpoint-д ашиглана
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}