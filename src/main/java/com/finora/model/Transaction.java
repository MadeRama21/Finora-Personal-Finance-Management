package com.finora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {

    private int id;
    private int categoryId;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private String categoryName;
    private CategoryType categoryType;

    public Transaction() {
    }

    public Transaction(int id, int categoryId, BigDecimal amount, String description,
                       LocalDate transactionDate, LocalDateTime createdAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    public Transaction(int id, int categoryId, BigDecimal amount, String description,
                       LocalDate transactionDate, LocalDateTime createdAt,
                       String categoryName, CategoryType categoryType) {
        this(id, categoryId, amount, description, transactionDate, createdAt);
        this.categoryName = categoryName;
        this.categoryType = categoryType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
}
