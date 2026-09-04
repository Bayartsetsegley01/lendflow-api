// src/main/java/com/lendflow/api/dto/RegisterRequest.java
package com.lendflow.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotNull(message = "monthlyIncome is required")
        @DecimalMin(value = "0.0", message = "monthlyIncome cannot be negative")
        BigDecimal monthlyIncome,

        @NotNull(message = "employmentMonths is required")
        Integer employmentMonths
) {}
