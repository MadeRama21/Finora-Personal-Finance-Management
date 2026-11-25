package com.finora.controller;

import com.finora.model.Category;
import com.finora.model.CategoryType;
import com.finora.model.Transaction;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionFormController {

    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField descriptionField;

    @FXML
    private Label formError;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<CategoryType> typeCombo;

    private List<Category> categories = List.of();
    private int transactionId;
    private boolean initializing;

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

        typeCombo.getItems().setAll(CategoryType.values());
        datePicker.setValue(LocalDate.now());
        formError.setVisible(false);
        formError.setManaged(false);

        typeCombo.valueProperty().addListener((observable, oldValue, value) -> {
            populateCategories(value);
            if (!initializing) {
                categoryCombo.setValue(null);
            }
        });
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        populateCategories(typeCombo.getValue());
    }

    public void setTransaction(Transaction transaction) {
        initializing = true;
        transactionId = transaction.getId();
        typeCombo.setValue(transaction.getCategoryType());
        categoryCombo.setValue(categories.stream()
                .filter(category -> category.getId() == transaction.getCategoryId())
                .findFirst().orElse(null));
        amountField.setText(transaction.getAmount().toPlainString());
        descriptionField.setText(transaction.getDescription());
        datePicker.setValue(transaction.getTransactionDate());
        initializing = false;
    }

    public boolean validate() {
        String error = null;
        BigDecimal amount = parseAmount(amountField.getText());

        if (typeCombo.getValue() == null) {
            error = "Pilih tipe transaksi.";
        } else if (categoryCombo.getValue() == null) {
            error = "Pilih kategori.";
        } else if (categoryCombo.getValue().getType() != typeCombo.getValue()) {
            error = "Kategori harus sesuai dengan tipe transaksi.";
        } else if (amount == null) {
            error = "Masukkan jumlah yang valid.";
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            error = "Jumlah harus lebih dari nol.";
        } else if (amount.scale() > 2) {
            error = "Jumlah maksimal 2 angka desimal.";
        } else if (amount.compareTo(MAX_AMOUNT) > 0) {
            error = "Jumlah terlalu besar.";
        } else if (datePicker.getValue() == null) {
            error = "Pilih tanggal transaksi.";
        } else if (text(descriptionField).length() > MAX_DESCRIPTION_LENGTH) {
            error = "Deskripsi maksimal 255 karakter.";
        }

        formError.setText(error == null ? "" : error);
        formError.setVisible(error != null);
        formError.setManaged(error != null);
        return error == null;
    }

    public Transaction getTransaction() {
        Category category = categoryCombo.getValue();
        String description = text(descriptionField).trim();
        return new Transaction(
                transactionId,
                category.getId(),
                parseAmount(amountField.getText()),
                description.isEmpty() ? null : description,
                datePicker.getValue(),
                null,
                category.getName(),
                category.getType()
        );
    }

    private void populateCategories(CategoryType type) {
        if (type == null) {
            categoryCombo.getItems().clear();
            return;
        }
        categoryCombo.getItems().setAll(categories.stream()
                .filter(category -> category.getType() == type)
                .toList());
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText();
    }
}
