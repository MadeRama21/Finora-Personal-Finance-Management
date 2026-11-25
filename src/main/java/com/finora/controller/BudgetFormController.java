package com.finora.controller;

import com.finora.model.Budget;
import com.finora.model.Category;
import com.finora.model.CategoryType;
import com.finora.util.MonthNames;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

public class BudgetFormController {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<Integer> monthCombo;

    @FXML
    private ComboBox<Integer> yearCombo;

    @FXML
    private Label formError;

    private List<Category> categories = List.of();
    private int budgetId;

    @FXML
    private void initialize() {
        categoryCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String value) {
                return null;
            }
        });

        monthCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer month) {
                return month == null ? "" : MonthNames.full(month);
            }

            @Override
            public Integer fromString(String value) {
                return null;
            }
        });
        monthCombo.getItems().addAll(java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList());

        int currentYear = Year.now().getValue();
        yearCombo.getItems().addAll(java.util.stream.IntStream
                .rangeClosed(currentYear - 2, currentYear + 2).boxed().toList());

        formError.setVisible(false);
        formError.setManaged(false);
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        categoryCombo.getItems().setAll(categories);
    }

    public void setDefaultPeriod(int month, int year) {
        monthCombo.setValue(month);
        if (!yearCombo.getItems().contains(year)) {
            yearCombo.getItems().add(year);
        }
        yearCombo.setValue(year);
    }

    public void setBudget(Budget budget) {
        budgetId = budget.getId();
        categoryCombo.setValue(categories.stream()
                .filter(category -> category.getId() == budget.getCategoryId())
                .findFirst().orElse(null));
        amountField.setText(budget.getAmount().toPlainString());
        monthCombo.setValue(budget.getMonth());
        if (!yearCombo.getItems().contains(budget.getYear())) {
            yearCombo.getItems().add(budget.getYear());
        }
        yearCombo.setValue(budget.getYear());
    }

    public boolean validate() {
        String error = null;
        BigDecimal amount = parseAmount(amountField.getText());
        Category category = categoryCombo.getValue();

        if (category == null) {
            error = "Pilih kategori pengeluaran.";
        } else if (category.getType() != CategoryType.EXPENSE) {
            error = "Anggaran hanya dapat dibuat untuk kategori pengeluaran.";
        } else if (amount == null) {
            error = "Masukkan jumlah yang valid.";
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            error = "Jumlah harus lebih dari nol.";
        } else if (amount.scale() > 2) {
            error = "Jumlah maksimal 2 angka desimal.";
        } else if (amount.compareTo(MAX_AMOUNT) > 0) {
            error = "Jumlah terlalu besar.";
        } else if (monthCombo.getValue() == null) {
            error = "Pilih bulan.";
        } else if (yearCombo.getValue() == null) {
            error = "Pilih tahun.";
        } else if (yearCombo.getValue() < MIN_YEAR || yearCombo.getValue() > MAX_YEAR) {
            error = "Tahun harus antara " + MIN_YEAR + " dan " + MAX_YEAR + ".";
        }

        formError.setText(error == null ? "" : error);
        formError.setVisible(error != null);
        formError.setManaged(error != null);
        return error == null;
    }

    public Budget getBudget() {
        return new Budget(
                budgetId,
                categoryCombo.getValue().getId(),
                parseAmount(amountField.getText()),
                monthCombo.getValue(),
                yearCombo.getValue()
        );
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
