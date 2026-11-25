package com.finora.model;

import java.math.BigDecimal;

public record MonthlyTotal(int year, int month, BigDecimal income, BigDecimal expense) {
}
