package com.finora.controller;

import com.finora.model.Category;
import com.finora.model.CategoryType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CategoryFormController {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_ICON_LENGTH = 50;

    @FXML
    private ComboBox<CategoryType> typeCombo;

    @FXML
    private TextField iconField;

    @FXML
    private TextField nameField;

    @FXML
    private Label formError;

    private int categoryId;

    @FXML
    private void initialize() {
        typeCombo.getItems().setAll(CategoryType.values());
        formError.setVisible(false);
        formError.setManaged(false);
    }

    public void setCategory(Category category) {
        categoryId = category.getId();
        nameField.setText(category.getName());
        typeCombo.setValue(category.getType());
        iconField.setText(category.getIcon());
    }

    public boolean validate() {
        String name = text(nameField).trim();
        String icon = text(iconField).trim();

        String error = null;
        if (name.isEmpty()) {
            error = "Nama kategori wajib diisi.";
        } else if (name.length() > MAX_NAME_LENGTH) {
            error = "Nama kategori maksimal 100 karakter.";
        } else if (typeCombo.getValue() == null) {
            error = "Pilih tipe kategori.";
        } else if (icon.length() > MAX_ICON_LENGTH) {
            error = "Ikon maksimal 50 karakter.";
        }

        formError.setText(error == null ? "" : error);
        formError.setVisible(error != null);
        formError.setManaged(error != null);
        return error == null;
    }

    public Category getCategory() {
        String icon = text(iconField).trim();
        return new Category(
                categoryId,
                text(nameField).trim(),
                typeCombo.getValue(),
                icon.isEmpty() ? null : icon
        );
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText();
    }
}
