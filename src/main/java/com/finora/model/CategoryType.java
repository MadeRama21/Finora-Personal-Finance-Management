package com.finora.model;

public enum CategoryType {
    INCOME,
    EXPENSE;

    @Override
    public String toString() {
        return this == INCOME ? "Pemasukan" : "Pengeluaran";
    }
}
