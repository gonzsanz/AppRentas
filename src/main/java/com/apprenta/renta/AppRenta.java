package com.apprenta.renta;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppRenta extends Application {
    @Override
    public void start(final Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        var root = loader.load();
        final Scene scene = new Scene((Parent) root, 1200, 700);
        stage.setTitle("App Renta");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
