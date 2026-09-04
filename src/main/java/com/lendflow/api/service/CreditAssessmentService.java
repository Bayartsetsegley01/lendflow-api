// src/main/java/com/lendflow/api/service/CreditAssessmentService.java
package com.lendflow.api.service;

import com.lendflow.api.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreditAssessmentService {

    // Rule-based оноо тооцоолол (0-100). Дараа нь илүү нарийвчилж болно,
    // гэхдээ portfolio-д зориулж энгийн, тайлбарлахад амар дүрэм ашиглав.
    public int calculateScore(User user) {
        int score = 50;

        if (user.getMonthlyIncome().compareTo(new BigDecimal("3000000")) >= 0) {
            score += 20;
        } else if (user.getMonthlyIncome().compareTo(new BigDecimal("1500000")) >= 0) {
            score += 10;
        }

        if (user.getEmploymentMonths() >= 24) {
            score += 15;
        } else if (user.getEmploymentMonths() >= 12) {
            score += 8;
        }

        // Хамгийн ихдээ 100-аас хэтрэхгүй байхаар хязгаарлана
        return Math.min(score, 100);
    }

    public String getRiskLevel(int score) {
        if (score >= 80) return "LOW";
        if (score >= 60) return "MEDIUM";
        return "HIGH";
    }

    // Зээлийн дүн хэрэглэгчийн сарын орлогоос хэт их байвал автоматаар татгална
    public boolean isAmountWithinLimit(User user, BigDecimal loanAmount) {
        BigDecimal maxLoan = user.getMonthlyIncome().multiply(BigDecimal.TEN);
        return loanAmount.compareTo(maxLoan) <= 0;
    }
}