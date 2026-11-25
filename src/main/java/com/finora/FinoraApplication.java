package com.finora;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FinoraApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(FinoraApplication.class.getResource("/com/finora/fxml/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(FinoraApplication.class.getResource("/com/finora/css/application.css").toExternalForm());

        stage.setTitle("Finora - Manajer Keuangan Pribadi");
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
