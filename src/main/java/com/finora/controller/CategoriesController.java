package com.finora.controller;

import com.finora.dao.CategoryDao;
import com.finora.model.Category;
import com.finora.model.CategoryType;
import com.finora.util.TableUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CategoriesController {

    private final CategoryDao categoryDao = new CategoryDao();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    @FXML
    private TableColumn<Category, String> actionsColumn;

    @FXML
    private TableColumn<Category, String> iconColumn;

    @FXML
    private TableColumn<Category, String> nameColumn;

    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private Label statusLabel;

    @FXML
    private TableColumn<Category, String> typeColumn;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        typeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType().toString()));
        iconColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getIcon() == null ? "" : cell.getValue().getIcon()));

        typeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().addAll("type-badge",
                        item.equals(CategoryType.INCOME.toString()) ? "income-badge" : "expense-badge");
                setGraphic(badge);
                setText(null);
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Ubah");
            private final Button deleteButton = new Button("Hapus");

            {
                editButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().addAll("table-action-button", "danger-button");

                editButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    openCategoryDialog(category);
                });
                deleteButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : TableUtils.centeredRow(editButton, deleteButton));
            }
        });

        categoryTable.setItems(categories);
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        categoryTable.setPlaceholder(new Label("Tidak ada kategori."));
    }

    public void reload() {
        statusLabel.setText("Memuat kategori...");

        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() throws SQLException {
                return categoryDao.findAll();
            }
        };
        task.setOnSucceeded(event -> {
            categories.setAll(task.getValue());
            statusLabel.setText(categories.size() + " kategori");
        });
        task.setOnFailed(event -> {
            statusLabel.setText("Gagal memuat kategori.");
            showError("Kesalahan Basis Data", "Gagal memuat kategori: " + message(task.getException()));
        });

        start(task, "finora-load-categories");
    }

    @FXML
    private void openAddCategoryDialog() {
        openCategoryDialog(null);
    }

    private void openCategoryDialog(Category category) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/finora/fxml/category-form.fxml"));
        DialogPane dialogPane;
        try {
            dialogPane = loader.load();
        } catch (IOException e) {
            showError("Kesalahan Formulir", "Tidak dapat membuka formulir kategori: " + message(e));
            return;
        }

        CategoryFormController form = loader.getController();
        boolean editing = category != null;
        if (editing) {
            form.setCategory(category);
        }

        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Ubah Kategori" : "Tambah Kategori");
        dialog.setHeaderText(null);
        dialog.setDialogPane(dialogPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        saveButton.setText(editing ? "Simpan" : "Tambah");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Batal");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!form.validate()) {
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType ->
                buttonType == ButtonType.OK ? form.getCategory() : null);

        dialog.showAndWait().ifPresent(value -> saveCategory(value, editing));
    }

    private void saveCategory(Category category, boolean editing) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws SQLException {
                return editing ? categoryDao.update(category) : categoryDao.insert(category);
            }
        };
        task.setOnSucceeded(event -> {
            reload();
            showInfo(editing ? "Kategori Diperbarui" : "Kategori Ditambahkan",
                    "'" + category.getName() + "' berhasil disimpan.");
        });
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal menyimpan kategori: " + message(task.getException())));

        start(task, "finora-save-category");
    }

    private void deleteCategory(Category category) {
        Task<Boolean> checkTask = new Task<>() {
            @Override
            protected Boolean call() throws SQLException {
                return categoryDao.isReferenced(category.getId());
            }
        };
        checkTask.setOnSucceeded(event -> {
            if (checkTask.getValue()) {
                showWarning("Kategori Tidak Dapat Dihapus",
                        "'" + category.getName() + "' digunakan oleh transaksi atau anggaran sehingga tidak dapat dihapus.");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Hapus '" + category.getName() + "'? Tindakan ini tidak dapat dibatalkan.",
                    ButtonType.OK, ButtonType.CANCEL);
            confirmation.setTitle("Hapus Kategori");
            ((Button) confirmation.getDialogPane().lookupButton(ButtonType.OK)).setText("Hapus");
            ((Button) confirmation.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Batal");
            confirmation.setHeaderText(null);
            confirmation.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    runDelete(category);
                }
            });
        });
        checkTask.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal memeriksa penggunaan kategori: " + message(checkTask.getException())));

        start(checkTask, "finora-check-category");
    }

    private void runDelete(Category category) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws SQLException {
                return categoryDao.delete(category.getId());
            }
        };
        task.setOnSucceeded(event -> {
            reload();
            showInfo("Kategori Dihapus", "'" + category.getName() + "' berhasil dihapus.");
        });
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal menghapus kategori: " + message(task.getException())));

        start(task, "finora-delete-category");
    }

    private void start(Task<?> task, String name) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }

    private String message(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
