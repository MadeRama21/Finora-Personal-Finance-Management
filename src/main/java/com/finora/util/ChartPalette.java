package com.finora.util;

import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

/**
 * Pins chart colors to the Finora palette. Chart series receive a rotating
 * default-color class from JavaFX, so the color index shifts after each refresh.
 * Setting the color on the node keeps the palette stable.
 */
public final class ChartPalette {

    private static final String INCOME_BAR = "-fx-bar-fill: #141413;";
    private static final String EXPENSE_BAR = "-fx-bar-fill: #CF4500;";
    private static final String[] PIE_COLORS = {
            "-fx-pie-color: #141413;",
            "-fx-pie-color: #CF4500;",
            "-fx-pie-color: #9A3A0A;",
            "-fx-pie-color: #F37338;",
            "-fx-pie-color: #696969;",
            "-fx-pie-color: #D1CDC7;"
    };

    private ChartPalette() {
    }

    public static void colorMonthly(XYChart.Series<String, Number> income, XYChart.Series<String, Number> expense) {
        apply(income, INCOME_BAR);
        apply(expense, EXPENSE_BAR);
    }

    public static void colorSlices(ObservableList<PieChart.Data> data) {
        for (int i = 0; i < data.size(); i++) {
            PieChart.Data slice = data.get(i);
            String style = PIE_COLORS[i % PIE_COLORS.length];
            if (slice.getNode() != null) {
                slice.getNode().setStyle(style);
            } else {
                slice.nodeProperty().addListener((observable, oldNode, node) -> {
                    if (node != null) {
                        node.setStyle(style);
                    }
                });
            }
        }
    }

    private static void apply(XYChart.Series<String, Number> series, String style) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle(style);
            } else {
                data.nodeProperty().addListener((observable, oldNode, node) -> {
                    if (node != null) {
                        node.setStyle(style);
                    }
                });
            }
        }
    }
}
