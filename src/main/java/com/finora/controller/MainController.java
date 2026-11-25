package com.finora.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", Locale.of("id", "ID"));

    @FXML
    private BudgetsController budgetsPageController;

    @FXML
    private Node budgetsPage;

    @FXML
    private Button budgetsButton;

    @FXML
    private CategoriesController categoriesPageController;

    @FXML
    private Node categoriesPage;

    @FXML
    private Button categoriesButton;

    @FXML
    private DashboardController dashboardPageController;

    @FXML
    private Node dashboardPage;

    @FXML
    private Button dashboardButton;

    @FXML
    private Label dateLabel;

    @FXML
    private Label pageSubtitle;

    @FXML
    private Label pageTitle;

    @FXML
    private ReportsController reportsPageController;

    @FXML
    private Node reportsPage;

    @FXML
    private Button reportsButton;

    @FXML
    private TransactionsController transactionsPageController;

    @FXML
    private Node transactionsPage;

    @FXML
    private Button transactionsButton;

    private List<Node> pages;
    private List<Button> navigationButtons;

    @FXML
    private void initialize() {
        dateLabel.setText(LocalDate.now().format(DATE_FORMATTER));

        pages = List.of(dashboardPage, transactionsPage, budgetsPage, categoriesPage, reportsPage);
        navigationButtons = List.of(
                dashboardButton,
                transactionsButton,
                budgetsButton,
                categoriesButton,
                reportsButton
        );

        showDashboard();
    }

    @FXML
    private void showBudgets() {
        showPage(budgetsPage, budgetsButton, "Anggaran", "Rencanakan dan pantau pengeluaran bulanan");
        budgetsPageController.reload();
    }

    @FXML
    private void showCategories() {
        showPage(categoriesPage, categoriesButton, "Kategori", "Kelola aktivitas keuangan Anda");
        categoriesPageController.reload();
    }

    @FXML
    private void showDashboard() {
        showPage(dashboardPage, dashboardButton, "Dasbor", "Ringkasan keuangan Anda");
        dashboardPageController.reload();
    }

    @FXML
    private void showReports() {
        showPage(reportsPage, reportsButton, "Laporan", "Pahami aktivitas keuangan Anda");
        reportsPageController.reload();
    }

    @FXML
    private void showTransactions() {
        showPage(transactionsPage, transactionsButton, "Transaksi", "Pantau pemasukan dan pengeluaran Anda");
        transactionsPageController.reload();
    }

    private void showPage(Node selectedPage, Button selectedButton, String title, String subtitle) {
        for (Node page : pages) {
            boolean isSelected = page == selectedPage;
            page.setVisible(isSelected);
            page.setManaged(isSelected);
        }

        for (Button button : navigationButtons) {
            button.getStyleClass().remove("selected");
        }
        selectedButton.getStyleClass().add("selected");

        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
    }
}
