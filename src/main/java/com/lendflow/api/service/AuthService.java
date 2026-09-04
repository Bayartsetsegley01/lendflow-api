// src/main/java/com/lendflow/api/service/AuthService.java
package com.lendflow.api.service;

import com.lendflow.api.config.JwtUtil;
import com.lendflow.api.dto.AuthResponse;
import com.lendflow.api.dto.LoginRequest;
import com.lendflow.api.dto.RegisterRequest;
import com.lendflow.api.entity.Account;
import com.lendflow.api.entity.User;
import com.lendflow.api.repository.AccountRepository;
import com.lendflow.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                        PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Хэрэглэгч бүртгүүлэхэд: User болон түүний Account (0 үлдэгдэлтэй) хоёуланг
    // нэг дор үүсгэнэ — @Transactional нь хоёулаа хамт амжилттай байхыг баталгаажуулна
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setMonthlyIncome(request.monthlyIncome());
        user.setEmploymentMonths(request.employmentMonths());
        userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        accountRepository.save(account);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}