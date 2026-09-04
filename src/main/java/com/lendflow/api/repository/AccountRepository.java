// src/main/java/com/lendflow/api/repository/AccountRepository.java
package com.lendflow.api.repository;

import com.lendflow.api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Хэрэглэгчийн (1 л байх ёстой) дансыг олоход ашиглана
    Optional<Account> findByUserId(Long userId);
}