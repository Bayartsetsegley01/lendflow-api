// src/main/java/com/lendflow/api/service/TransactionService.java
package com.lendflow.api.service;

import com.lendflow.api.entity.Transaction;
import com.lendflow.api.entity.User;
import com.lendflow.api.exception.UserNotFoundException;
import com.lendflow.api.repository.TransactionRepository;
import com.lendflow.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // Нэвтэрсэн хэрэглэгчийн бүх гүйлгээ (DISBURSEMENT + REPAYMENT), шинэ нь эхэнд
    public List<Transaction> getMyTransactions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}
