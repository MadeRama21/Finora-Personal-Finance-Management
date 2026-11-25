package com.finora.model;

import java.math.BigDecimal;
import java.util.List;

public record DashboardData(BigDecimal income,
                            BigDecimal expense,
                            List<CategorySummary> expenseByCategory,
                            List<MonthlyTotal> monthlyTotals,
                            List<Transaction> recentTransactions) {

    public BigDecimal balance() {
        return income.subtract(expense);
    }
}
