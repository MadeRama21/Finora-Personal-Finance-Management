package com.finora.util;

import javafx.scene.control.DialogPane;

/**
 * Applies the application stylesheet to dialogs, which open in their own window
 * and do not inherit the main scene stylesheet.
 */
public final class DialogStyles {

    private static final String STYLESHEET = DialogStyles.class
            .getResource("/com/finora/css/application.css").toExternalForm();

    private DialogStyles() {
    }

    public static void apply(DialogPane pane) {
        if (!pane.getStylesheets().contains(STYLESHEET)) {
            pane.getStylesheets().add(STYLESHEET);
        }
        pane.setGraphic(null);
    }
}
