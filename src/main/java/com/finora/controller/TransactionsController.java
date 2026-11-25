package com.finora.controller;

import com.finora.dao.CategoryDao;
import com.finora.dao.TransactionDao;
import com.finora.model.Category;
import com.finora.model.CategoryType;
import com.finora.model.Transaction;
import com.finora.util.CurrencyFormatter;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionsController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.of("id", "ID"));

    private final CategoryDao categoryDao = new CategoryDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    @FXML
    private TableColumn<Transaction, String> actionsColumn;

    @FXML
    private TableColumn<Transaction, String> amountColumn;

    @FXML
    private ComboBox<Category> categoryFilter;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    @FXML
    private TableColumn<Transaction, String> descriptionColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private ComboBox<CategoryType> typeFilter;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private void initialize() {
        typeFilter.getItems().add(null);
        typeFilter.getItems().addAll(CategoryType.values());
        typeFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoryType type) {
                return type == null ? "Semua tipe" : type.toString();
            }

            @Override
            public CategoryType fromString(String value) {
                return null;
            }
        });
        categoryFilter.getItems().add(null);
        categoryFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "Semua kategori" : category.getName();
            }

            @Override
            public Category fromString(String value) {
                return null;
            }
        });

        dateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTransactionDate().format(DATE_FORMATTER)));
        descriptionColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescription() == null
                        ? "—" : cell.getValue().getDescription()));
        categoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCategoryName()));
        typeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCategoryType().toString()));
        amountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().getAmount())));

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

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("amount-income", "amount-expense");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                Transaction transaction = getTableRow() == null ? null : (Transaction) getTableRow().getItem();
                if (transaction != null && transaction.getCategoryType() == CategoryType.INCOME) {
                    getStyleClass().add("amount-income");
                } else {
                    getStyleClass().add("amount-expense");
                }
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Ubah");
            private final Button deleteButton = new Button("Hapus");

            {
                editButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().addAll("table-action-button", "danger-button");

                editButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    openTransactionDialog(transaction);
                });
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : TableUtils.centeredRow(editButton, deleteButton));
            }
        });

        transactionTable.setItems(transactions);
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        transactionTable.setPlaceholder(new Label("Tidak ada transaksi."));

        reload();
        loadCategories();
    }

    public void reload() {
        String search = text(searchField);
        CategoryType type = typeFilter.getValue();
        Category selectedCategory = categoryFilter.getValue();
        Integer categoryId = selectedCategory == null ? null : selectedCategory.getId();
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        statusLabel.setText("Memuat transaksi...");

        Task<List<Transaction>> task = new Task<>() {
            @Override
            protected List<Transaction> call() throws SQLException {
                return transactionDao.findAll(search, type, categoryId, from, to);
            }
        };
        task.setOnSucceeded(event -> {
            transactions.setAll(task.getValue());
            statusLabel.setText(transactions.size() + " transaksi");
        });
        task.setOnFailed(event -> {
            statusLabel.setText("Gagal memuat transaksi.");
            showError("Kesalahan Basis Data", "Gagal memuat transaksi: " + message(task.getException()));
        });

        start(task, "finora-load-transactions");
    }

    private void loadCategories() {
        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() throws SQLException {
                return categoryDao.findAll();
            }
        };
        task.setOnSucceeded(event -> {
            categories.setAll(task.getValue());
            categoryFilter.getItems().addAll(categories);
        });
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal memuat kategori: " + message(task.getException())));

        start(task, "finora-load-categories-for-transactions");
    }

    @FXML
    private void applyFilters() {
        if (dateFrom.getValue() != null && dateTo.getValue() != null
                && dateFrom.getValue().isAfter(dateTo.getValue())) {
            showWarning("Periode Tidak Valid", "Tanggal mulai harus sebelum tanggal akhir.");
            return;
        }
        reload();
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        typeFilter.setValue(null);
        categoryFilter.setValue(null);
        dateFrom.setValue(null);
        dateTo.setValue(null);
        reload();
    }

    @FXML
    private void openAddTransactionDialog() {
        openTransactionDialog(null);
    }

    private void openTransactionDialog(Transaction transaction) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/finora/fxml/transaction-form.fxml"));
        DialogPane dialogPane;
        try {
            dialogPane = loader.load();
        } catch (IOException e) {
            showError("Kesalahan Formulir", "Tidak dapat membuka formulir transaksi: " + message(e));
            return;
        }

        TransactionFormController form = loader.getController();
        form.setCategories(new ArrayList<>(categories));
        boolean editing = transaction != null;
        if (editing) {
            form.setTransaction(transaction);
        }

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Ubah Transaksi" : "Tambah Transaksi");
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
                buttonType == ButtonType.OK ? form.getTransaction() : null);

        dialog.showAndWait().ifPresent(value -> saveTransaction(value, editing));
    }

    private void saveTransaction(Transaction transaction, boolean editing) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws SQLException {
                return editing ? transactionDao.update(transaction) : transactionDao.insert(transaction);
            }
        };
        task.setOnSucceeded(event -> {
            reload();
            showInfo(editing ? "Transaksi Diperbarui" : "Transaksi Ditambahkan",
                    "Transaksi berhasil disimpan.");
        });
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal menyimpan transaksi: " + message(task.getException())));

        start(task, "finora-save-transaction");
    }

    private void deleteTransaction(Transaction transaction) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus transaksi ini? Tindakan ini tidak dapat dibatalkan.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Hapus Transaksi");
        ((Button) confirmation.getDialogPane().lookupButton(ButtonType.OK)).setText("Hapus");
        ((Button) confirmation.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Batal");
        confirmation.setHeaderText(null);
        confirmation.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                runDelete(transaction);
            }
        });
    }

    private void runDelete(Transaction transaction) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws SQLException {
                return transactionDao.delete(transaction.getId());
            }
        };
        task.setOnSucceeded(event -> {
            reload();
            showInfo("Transaksi Dihapus", "Transaksi berhasil dihapus.");
        });
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal menghapus transaksi: " + message(task.getException())));

        start(task, "finora-delete-transaction");
    }

    private void start(Task<?> task, String name) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
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
