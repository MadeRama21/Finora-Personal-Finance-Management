package com.finora.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record BudgetSummary(Budget budget, String categoryName, BigDecimal spent) {

    public BigDecimal remaining() {
        return budget.getAmount().subtract(spent);
    }

    public boolean isOverBudget() {
        return remaining().compareTo(BigDecimal.ZERO) < 0;
    }

    public double progress() {
        BigDecimal amount = budget.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        double ratio = spent.divide(amount, 4, RoundingMode.HALF_UP).doubleValue();
        return Math.max(0, Math.min(1, ratio));
    }

    public String progressText() {
        BigDecimal amount = budget.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "0%";
        }
        int percent = spent.multiply(BigDecimal.valueOf(100))
                .divide(amount, 0, RoundingMode.HALF_UP)
                .intValue();
        return percent + "%";
    }
}
