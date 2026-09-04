package com.lendflow.api.service;

import com.lendflow.api.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

// Цэвэр unit test — dependency байхгүй тул mock шаардлагагүй, зөвхөн rule-based логик шалгана
class CreditAssessmentServiceTest {

    private final CreditAssessmentService service = new CreditAssessmentService();

    private User user(String monthlyIncome, int employmentMonths) {
        User u = new User();
        u.setMonthlyIncome(new BigDecimal(monthlyIncome));
        u.setEmploymentMonths(employmentMonths);
        return u;
    }

    @Test
    void highIncomeAndLongEmployment_givesHighScore() {
        // 50 (base) + 20 (income >= 3M) + 15 (employment >= 24) = 85
        int score = service.calculateScore(user("3000000", 24));
        assertThat(score).isGreaterThanOrEqualTo(80);
    }

    @Test
    void lowIncomeAndShortEmployment_givesLowScore() {
        // 50 (base) + 0 + 0 = 50
        int score = service.calculateScore(user("800000", 3));
        assertThat(score).isLessThan(60);
    }

    @Test
    void score_isNeverAbove100() {
        int score = service.calculateScore(user("99999999", 240));
        assertThat(score).isLessThanOrEqualTo(100);
    }

    @Test
    void getRiskLevel_mapsScoreToBucket() {
        assertThat(service.getRiskLevel(80)).isEqualTo("LOW");
        assertThat(service.getRiskLevel(95)).isEqualTo("LOW");
        assertThat(service.getRiskLevel(79)).isEqualTo("MEDIUM");
        assertThat(service.getRiskLevel(60)).isEqualTo("MEDIUM");
        assertThat(service.getRiskLevel(59)).isEqualTo("HIGH");
        assertThat(service.getRiskLevel(0)).isEqualTo("HIGH");
    }

    @Test
    void isAmountWithinLimit_rejectsLoanOverTenTimesIncome() {
        User u = user("1000000", 12); // max allowed = 10,000,000
        assertThat(service.isAmountWithinLimit(u, new BigDecimal("10000001"))).isFalse();
        assertThat(service.isAmountWithinLimit(u, new BigDecimal("11000000"))).isFalse();
    }

    @Test
    void isAmountWithinLimit_allowsLoanWithinTenTimesIncome() {
        User u = user("1000000", 12);
        assertThat(service.isAmountWithinLimit(u, new BigDecimal("5000000"))).isTrue();
        assertThat(service.isAmountWithinLimit(u, new BigDecimal("10000000"))).isTrue();
    }
}
