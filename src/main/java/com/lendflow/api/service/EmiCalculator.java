// src/main/java/com/lendflow/api/service/EmiCalculator.java
package com.lendflow.api.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

// Сар бүрийн тэнцүү төлбөрийн дүнг (EMI) тооцоолох туслах класс
@Component
public class EmiCalculator {

    public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRatePercent, int termMonths) {

        // Жилийн хүүг сарын хүү болгож хувиргана: 18% / 12 / 100 = 0.015
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(12), MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);

        // (1 + r)^n тооцоолол
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal factor = onePlusR.pow(termMonths, MathContext.DECIMAL64);

        // M = P × r × factor / (factor - 1)
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(factor);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}