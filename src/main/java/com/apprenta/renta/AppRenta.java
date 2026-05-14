package com.apprenta.renta;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppRenta extends Application {
    @Override
    public void start(final Stage stage) {
        final BorderPane root = new BorderPane();
        root.setCenter(new Label("Gestión de Autónomos"));
        final Scene scene = new Scene(root, 1200, 700);
        stage.setTitle("App Renta");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
