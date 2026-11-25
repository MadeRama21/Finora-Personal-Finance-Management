package com.finora.controller;

import com.finora.dao.TransactionDao;
import com.finora.model.CategorySummary;
import com.finora.model.DashboardData;
import com.finora.model.MonthlyTotal;
import com.finora.model.Transaction;
import com.finora.util.ChartPalette;
import com.finora.util.CurrencyFormatter;
import com.finora.util.DialogStyles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    private static final int RECENT_LIMIT = 6;
    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MMM uuuu", Locale.of("id", "ID"));
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.of("id", "ID"));

    private final TransactionDao transactionDao = new TransactionDao();
    private final ObservableList<Transaction> recentTransactions = FXCollections.observableArrayList();

    @FXML
    private BarChart<String, Number> incomeExpenseChart;

    @FXML
    private PieChart expenseChart;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private Label balanceLabel;

    @FXML
    private Label expenseLabel;

    @FXML
    private Label incomeLabel;

    @FXML
    private TableColumn<Transaction, String> recentAmountColumn;

    @FXML
    private TableColumn<Transaction, String> recentCategoryColumn;

    @FXML
    private TableColumn<Transaction, String> recentDateColumn;

    @FXML
    private TableColumn<Transaction, String> recentDescriptionColumn;

    @FXML
    private TableView<Transaction> recentTable;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        recentDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTransactionDate().format(DATE_FORMATTER)));
        recentDescriptionColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDescription() == null
                        ? "—" : cell.getValue().getDescription()));
        recentCategoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCategoryName()));
        recentAmountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().getAmount())));
        recentAmountColumn.setCellFactory(column -> {
            TableCell<Transaction, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.getStyleClass().add("align-right");
            return cell;
        });

        recentTable.setItems(recentTransactions);
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        recentTable.setPlaceholder(new Label("Tidak ada transaksi untuk periode ini."));
    }

    public void reload() {
        LocalDate from = effectiveFrom();
        LocalDate to = effectiveTo();

        statusLabel.setText("Memuat ringkasan...");

        Task<DashboardData> task = new Task<>() {
            @Override
            protected DashboardData call() throws SQLException {
                return transactionDao.getDashboardData(from, to, RECENT_LIMIT);
            }
        };
        task.setOnSucceeded(event -> render(task.getValue(), from, to));
        task.setOnFailed(event -> {
            statusLabel.setText("Gagal memuat data dasbor.");
            showError("Kesalahan Basis Data", "Gagal memuat data dasbor: " + message(task.getException()));
        });

        Thread thread = new Thread(task, "finora-load-dashboard");
        thread.setDaemon(true);
        thread.start();
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
        dateFrom.setValue(null);
        dateTo.setValue(null);
        reload();
    }

    private void render(DashboardData data, LocalDate from, LocalDate to) {
        balanceLabel.setText(CurrencyFormatter.format(data.balance()));
        incomeLabel.setText(CurrencyFormatter.format(data.income()));
        expenseLabel.setText(CurrencyFormatter.format(data.expense()));
        statusLabel.setText("Periode: " + from.format(DATE_FORMATTER) + " – " + to.format(DATE_FORMATTER));

        renderExpenseChart(data.expenseByCategory());
        renderMonthlyChart(data.monthlyTotals());
        recentTransactions.setAll(data.recentTransactions());
    }

    private void renderExpenseChart(List<CategorySummary> summaries) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        List<BigDecimal> amounts = new ArrayList<>();

        for (CategorySummary summary : summaries) {
            pieData.add(new PieChart.Data(summary.categoryName(), summary.amount().doubleValue()));
            amounts.add(summary.amount());
        }
        expenseChart.setData(pieData);
        ChartPalette.colorSlices(pieData);

        for (int i = 0; i < pieData.size(); i++) {
            PieChart.Data slice = pieData.get(i);
            BigDecimal amount = amounts.get(i);
            slice.nodeProperty().addListener((observable, oldNode, node) -> {
                if (node != null) {
                    Tooltip.install(node, new Tooltip(slice.getName() + ": " + CurrencyFormatter.format(amount)));
                }
            });
        }
    }

    private void renderMonthlyChart(List<MonthlyTotal> totals) {
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Pemasukan");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Pengeluaran");

        for (MonthlyTotal total : totals) {
            String label = YearMonth.of(total.year(), total.month()).format(MONTH_FORMATTER);
            incomeSeries.getData().add(new XYChart.Data<>(label, total.income().doubleValue()));
            expenseSeries.getData().add(new XYChart.Data<>(label, total.expense().doubleValue()));
        }
        incomeExpenseChart.getData().setAll(List.of(incomeSeries, expenseSeries));
        ChartPalette.colorMonthly(incomeSeries, expenseSeries);
    }

    private LocalDate effectiveFrom() {
        LocalDate selected = dateFrom.getValue();
        return selected != null ? selected : LocalDate.now().withDayOfMonth(1).minusMonths(5);
    }

    private LocalDate effectiveTo() {
        LocalDate selected = dateTo.getValue();
        return selected != null ? selected : LocalDate.now();
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

    private void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        DialogStyles.apply(alert.getDialogPane());
        alert.showAndWait();
    }
}
