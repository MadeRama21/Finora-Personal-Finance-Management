package com.finora.controller;

import com.finora.dao.BudgetDao;
import com.finora.dao.CategoryDao;
import com.finora.model.Budget;
import com.finora.model.BudgetSummary;
import com.finora.model.Category;
import com.finora.model.CategoryType;
import com.finora.util.CurrencyFormatter;
import com.finora.util.MonthNames;
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
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class BudgetsController {

    private final BudgetDao budgetDao = new BudgetDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ObservableList<BudgetSummary> budgets = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    @FXML
    private TableColumn<BudgetSummary, String> actionsColumn;

    @FXML
    private TableColumn<BudgetSummary, String> amountColumn;

    @FXML
    private TableColumn<BudgetSummary, String> categoryColumn;

    @FXML
    private ComboBox<Integer> monthFilter;

    @FXML
    private TableColumn<BudgetSummary, String> periodColumn;

    @FXML
    private TableColumn<BudgetSummary, String> progressColumn;

    @FXML
    private TableColumn<BudgetSummary, String> remainingColumn;

    @FXML
    private TableColumn<BudgetSummary, String> spentColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<BudgetSummary> budgetTable;

    @FXML
    private ComboBox<Integer> yearFilter;

    @FXML
    private void initialize() {
        monthFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer month) {
                return month == null ? "" : MonthNames.full(month);
            }

            @Override
            public Integer fromString(String value) {
                return null;
            }
        });
        monthFilter.getItems().addAll(IntStream.rangeClosed(1, 12).boxed().toList());
        int currentYear = Year.now().getValue();
        yearFilter.getItems().addAll(IntStream.rangeClosed(currentYear - 2, currentYear + 2).boxed().toList());
        monthFilter.setValue(java.time.LocalDate.now().getMonthValue());
        yearFilter.setValue(currentYear);

        categoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().categoryName()));
        periodColumn.setCellValueFactory(cell -> new SimpleStringProperty(periodOf(cell.getValue())));
        amountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().budget().getAmount())));
        spentColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().spent())));
        remainingColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().remaining())));

        remainingColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("over-budget");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                BudgetSummary summary = getTableRow() == null ? null : (BudgetSummary) getTableRow().getItem();
                if (summary != null && summary.isOverBudget()) {
                    getStyleClass().add("over-budget");
                }
            }
        });

        progressColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                BudgetSummary summary = (BudgetSummary) getTableRow().getItem();
                ProgressBar bar = new ProgressBar(summary.progress());
                bar.setPrefWidth(110);
                bar.setMaxWidth(110);
                bar.getStyleClass().add(summary.isOverBudget() ? "over-budget-bar" : "budget-bar");
                Label percent = new Label(summary.progressText());
                percent.getStyleClass().add("progress-label");
                setGraphic(TableUtils.centeredRow(bar, percent));
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Ubah");
            private final Button deleteButton = new Button("Hapus");

            {
                editButton.getStyleClass().add("table-action-button");
                deleteButton.getStyleClass().addAll("table-action-button", "danger-button");

                editButton.setOnAction(event -> {
                    BudgetSummary summary = getTableView().getItems().get(getIndex());
                    openBudgetDialog(summary);
                });
                deleteButton.setOnAction(event -> {
                    BudgetSummary summary = getTableView().getItems().get(getIndex());
                    deleteBudget(summary);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : TableUtils.centeredRow(editButton, deleteButton));
            }
        });

        budgetTable.setItems(budgets);
        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        budgetTable.setPlaceholder(new Label("Tidak ada anggaran untuk periode ini."));

        reload();
        loadCategories();
    }

    public void reload() {
        int month = monthFilter.getValue() == null ? java.time.LocalDate.now().getMonthValue() : monthFilter.getValue();
        int year = yearFilter.getValue() == null ? Year.now().getValue() : yearFilter.getValue();
        String period = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.of("id", "ID")) + " " + year;

        statusLabel.setText("Memuat anggaran...");

        Task<List<BudgetSummary>> task = new Task<>() {
            @Override
            protected List<BudgetSummary> call() throws SQLException {
                return budgetDao.findAll(month, year);
            }
        };
        task.setOnSucceeded(event -> {
            budgets.setAll(task.getValue());
            statusLabel.setText(budgets.size() + " anggaran untuk " + period);
        });
        task.setOnFailed(event -> {
            statusLabel.setText("Gagal memuat anggaran.");
            showError("Kesalahan Basis Data", "Gagal memuat anggaran: " + message(task.getException()));
        });

        start(task, "finora-load-budgets");
    }

    private void loadCategories() {
        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() throws SQLException {
                return categoryDao.findAll().stream()
                        .filter(category -> category.getType() == CategoryType.EXPENSE)
                        .toList();
            }
        };
        task.setOnSucceeded(event -> categories.setAll(task.getValue()));
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal memuat kategori: " + message(task.getException())));

        start(task, "finora-load-categories-for-budgets");
    }

    @FXML
    private void applyFilters() {
        reload();
    }

    @FXML
    private void openAddBudgetDialog() {
        openBudgetDialog(null);
    }

    private void openBudgetDialog(BudgetSummary summary) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/finora/fxml/budget-form.fxml"));
        DialogPane dialogPane;
        try {
            dialogPane = loader.load();
        } catch (IOException e) {
            showError("Kesalahan Formulir", "Tidak dapat membuka formulir anggaran: " + message(e));
            return;
        }

        BudgetFormController form = loader.getController();
        form.setCategories(new java.util.ArrayList<>(categories));
        boolean editing = summary != null;
        if (editing) {
            form.setBudget(summary.budget());
        } else {
            form.setDefaultPeriod(
                    monthFilter.getValue() == null ? java.time.LocalDate.now().getMonthValue() : monthFilter.getValue(),
                    yearFilter.getValue() == null ? Year.now().getValue() : yearFilter.getValue()
            );
        }

        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Ubah Anggaran" : "Tambah Anggaran");
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

        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK ? form.getBudget() : null);
        dialog.showAndWait().ifPresent(value -> saveBudget(value, editing));
    }

    private void saveBudget(Budget budget, boolean editing) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws SQLException {
                if (budgetDao.existsDuplicate(budget.getCategoryId(), budget.getMonth(),
                        budget.getYear(), budget.getId())) {
                    throw new IllegalStateException("DUPLICATE_BUDGET");
                }
                if (editing) {
                    budgetDao.update(budget);
                } else {
                    budgetDao.insert(budget);
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            reload();
            showInfo(editing ? "Anggaran Diperbarui" : "Anggaran Ditambahkan", "Anggaran berhasil disimpan.");
        });
        task.setOnFailed(event -> {
            Throwable root = rootCause(task.getException());
            if (root instanceof SQLIntegrityConstraintViolationException) {
                showWarning("Anggaran Tidak Tersimpan",
                        "Basis data hanya mengizinkan satu anggaran per kategori. "
                                + "Perbarui anggaran yang ada atau ubah bulan/tahunnya.");
            } else if ("DUPLICATE_BUDGET".equals(root.getMessage())) {
                showWarning("Anggaran Tidak Tersimpan",
                        "Kategori ini sudah memiliki anggaran untuk bulan dan tahun tersebut.");
            } else {
                showError("Kesalahan Basis Data", "Gagal menyimpan anggaran: " + message(task.getException()));
            }
        });

        start(task, "finora-save-budget");
    }

    private void deleteBudget(BudgetSummary summary) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus anggaran untuk '" + summary.categoryName() + "'? Tindakan ini tidak dapat dibatalkan.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Hapus Anggaran");
        ((Button) confirmation.getDialogPane().lookupButton(ButtonType.OK)).setText("Hapus");
        ((Button) confirmation.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Batal");
        confirmation.setHeaderText(null);
        confirmation.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                runDelete(summary.budget());
            }
        });
    }

    private void runDelete(Budget budget) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws SQLException {
                return budgetDao.delete(budget.getId());
            }
        };
        task.setOnSucceeded(event -> {
            reload();
            showInfo("Anggaran Dihapus", "Anggaran berhasil dihapus.");
        });
        task.setOnFailed(event -> showError("Kesalahan Basis Data",
                "Gagal menghapus anggaran: " + message(task.getException())));

        start(task, "finora-delete-budget");
    }

    private String periodOf(BudgetSummary summary) {
        Budget budget = summary.budget();
        return Month.of(budget.getMonth()).getDisplayName(TextStyle.SHORT, Locale.of("id", "ID"))
                + " " + budget.getYear();
    }

    private void start(Task<?> task, String name) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }

    private Throwable rootCause(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }

    private String message(Throwable error) {
        Throwable root = rootCause(error);
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
