package com.finora.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public final class TableUtils {

    private TableUtils() {
    }

    public static HBox centeredRow(Node... nodes) {
        HBox box = new HBox(8, nodes);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }
}
