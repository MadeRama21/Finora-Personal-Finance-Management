package com.finora.controller;

import com.finora.dao.TransactionDao;
import com.finora.model.CategorySummary;
import com.finora.model.DashboardData;
import com.finora.model.MonthlyTotal;
import com.finora.util.CurrencyFormatter;
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
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReportsController {

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MMM uuuu", Locale.of("id", "ID"));
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.of("id", "ID"));

    private final TransactionDao transactionDao = new TransactionDao();
    private final ObservableList<MonthlyTotal> monthlyTotals = FXCollections.observableArrayList();

    @FXML
    private BarChart<String, Number> monthlyChart;

    @FXML
    private PieChart categoryChart;

    @FXML
    private DatePicker dateFrom;

    @FXML
    private DatePicker dateTo;

    @FXML
    private Label expenseLabel;

    @FXML
    private Label incomeLabel;

    @FXML
    private TableColumn<MonthlyTotal, String> monthlyExpenseColumn;

    @FXML
    private TableColumn<MonthlyTotal, String> monthlyIncomeColumn;

    @FXML
    private TableColumn<MonthlyTotal, String> monthlyMonthColumn;

    @FXML
    private TableColumn<MonthlyTotal, String> monthlyNetColumn;

    @FXML
    private TableColumn<MonthlyTotal, String> monthlySavingsColumn;

    @FXML
    private TableView<MonthlyTotal> monthlyTable;

    @FXML
    private Label netLabel;

    @FXML
    private Label savingsLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        monthlyMonthColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                YearMonth.of(cell.getValue().year(), cell.getValue().month()).format(MONTH_FORMATTER)));
        monthlyIncomeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().income())));
        monthlyExpenseColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(cell.getValue().expense())));
        monthlyNetColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(CurrencyFormatter.format(netOf(cell.getValue()))));
        monthlySavingsColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(savingsRate(cell.getValue().income(), netOf(cell.getValue()))));

        monthlyNetColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("amount-income", "amount-expense");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                MonthlyTotal total = getTableRow() == null ? null : (MonthlyTotal) getTableRow().getItem();
                getStyleClass().add(netOf(total).compareTo(BigDecimal.ZERO) < 0
                        ? "amount-expense" : "amount-income");
            }
        });

        monthlyTable.setItems(monthlyTotals);
        monthlyTable.setPlaceholder(new Label("Tidak ada data bulanan untuk periode ini."));

        reload();
    }

    public void reload() {
        LocalDate from = effectiveFrom();
        LocalDate to = effectiveTo();

        statusLabel.setText("Memuat laporan...");

        Task<ReportData> task = new Task<>() {
            @Override
            protected ReportData call() throws SQLException {
                return new ReportData(
                        transactionDao.getDashboardData(from, to, 0),
                        transactionDao.countTransactions(from, to)
                );
            }
        };
        task.setOnSucceeded(event -> render(task.getValue(), from, to));
        task.setOnFailed(event -> {
            statusLabel.setText("Gagal memuat laporan.");
            showError("Kesalahan Basis Data", "Gagal memuat laporan: " + message(task.getException()));
        });

        Thread thread = new Thread(task, "finora-load-report");
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

    private void render(ReportData data, LocalDate from, LocalDate to) {
        DashboardData dashboard = data.dashboard();
        BigDecimal income = dashboard.income();
        BigDecimal expense = dashboard.expense();
        BigDecimal net = dashboard.balance();

        incomeLabel.setText(CurrencyFormatter.format(income));
        expenseLabel.setText(CurrencyFormatter.format(expense));
        netLabel.setText(CurrencyFormatter.format(net));
        netLabel.getStyleClass().removeAll("amount-income", "amount-expense");
        netLabel.getStyleClass().add(net.compareTo(BigDecimal.ZERO) < 0 ? "amount-expense" : "amount-income");
        savingsLabel.setText(savingsRate(income, net));

        statusLabel.setText("Periode: " + from.format(DATE_FORMATTER) + " – " + to.format(DATE_FORMATTER)
                + "  •  " + data.transactionCount() + " transaksi");

        renderMonthlyChart(dashboard.monthlyTotals());
        renderCategoryChart(dashboard.expenseByCategory());
        monthlyTotals.setAll(dashboard.monthlyTotals());
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
        monthlyChart.getData().setAll(List.of(incomeSeries, expenseSeries));
    }

    private void renderCategoryChart(List<CategorySummary> summaries) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        List<BigDecimal> amounts = new java.util.ArrayList<>();

        for (CategorySummary summary : summaries) {
            pieData.add(new PieChart.Data(summary.categoryName(), summary.amount().doubleValue()));
            amounts.add(summary.amount());
        }
        categoryChart.setData(pieData);

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

    private BigDecimal netOf(MonthlyTotal total) {
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return total.income().subtract(total.expense());
    }

    private String savingsRate(BigDecimal income, BigDecimal net) {
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return "0%";
        }
        return net.multiply(BigDecimal.valueOf(100))
                .divide(income, 0, RoundingMode.HALF_UP) + "%";
    }

    private LocalDate effectiveFrom() {
        LocalDate selected = dateFrom.getValue();
        return selected != null ? selected : LocalDate.of(Year.now().getValue(), 1, 1);
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
        alert.showAndWait();
    }

    private record ReportData(DashboardData dashboard, int transactionCount) {
    }
}
