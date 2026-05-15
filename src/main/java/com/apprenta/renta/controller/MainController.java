package com.apprenta.renta.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnInicio;
    @FXML private Button btnIngresos;
    @FXML private Button btnGastos;
    @FXML private Button btnModelo303;
    @FXML private Button btnModelo130;
    @FXML private Label lblBBDD;

    private Button activeBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        activeBtn = btnInicio;
        navegarInicio();
    }

    @FXML private void navegarInicio() {
        cargarVista("/fxml/InicioView.fxml");
        setActive(btnInicio);
    }

    @FXML private void navegarIngresos() {
        cargarVista("/fxml/IngresosView.fxml");
        setActive(btnIngresos);
    }

    @FXML private void navegarGastos() {
        cargarVista("/fxml/GastosView.fxml");
        setActive(btnGastos);
    }

    @FXML private void navegarModelo303() {
        cargarVista("/fxml/Modelo303View.fxml");
        setActive(btnModelo303);
    }

    @FXML private void navegarModelo130() {
        cargarVista("/fxml/Modelo130View.fxml");
        setActive(btnModelo130);
    }

    private void cargarVista(String fxmlPath) {
        try {
            var loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button btn) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("nav-btn-active");
        }
        btn.getStyleClass().add("nav-btn-active");
        activeBtn = btn;
    }
}