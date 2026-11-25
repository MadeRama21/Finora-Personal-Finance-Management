package com.finora.model;

import java.math.BigDecimal;

public class Budget {

    private int id;
    private int categoryId;
    private BigDecimal amount;
    private int month;
    private int year;

    public Budget() {
    }

    public Budget(int id, int categoryId, BigDecimal amount, int month, int year) {
        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
