package com.apprenta.renta.controller;

import com.apprenta.renta.model.Expense;
import com.apprenta.renta.repository.ExpenseRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class GastosController implements Initializable {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Expense> tablaGastos;
    @FXML private TableColumn<Expense, String> colFecha;
    @FXML private TableColumn<Expense, String> colProveedor;
    @FXML private TableColumn<Expense, String> colConcepto;
    @FXML private TableColumn<Expense, String> colNaturaleza;
    @FXML private TableColumn<Expense, String> colBase;
    @FXML private TableColumn<Expense, String> colIva;
    @FXML private TableColumn<Expense, String> colTotal;
    @FXML private Label lblConteo;

    private final ExpenseRepository repo = new ExpenseRepository();
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final ObservableList<Expense> datos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        tablaGastos.setItems(datos);
        cbCategoria.getItems().addAll(
                "Todas las categorías", "EXISTENCIAS", "FAC. LUZ", "FAC. TELEFONO",
                "REPARACION Y CONSERVACION", "OTROS");
        cbCategoria.setValue("Todas las categorías");

        int mes = LocalDate.now().getMonthValue();
        int tri = (mes - 1) / 3 + 1;
        int mesInicio = (tri - 1) * 3 + 1;
        dpDesde.setValue(LocalDate.of(LocalDate.now().getYear(), mesInicio, 1));
        dpHasta.setValue(LocalDate.of(LocalDate.now().getYear(), mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(LocalDate.now().getYear(), mesInicio + 2, 1).lengthOfMonth()));

        cargarDatos();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha().toString()));
        colProveedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProveedor()));
        colConcepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumFactura()));
        colNaturaleza.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaturaleza()));
        colBase.setCellValueFactory(c -> new SimpleStringProperty(fmt.format(c.getValue().getBase())));
        colIva.setCellValueFactory(c -> new SimpleStringProperty(fmt.format(c.getValue().getCuotaIva())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(fmt.format(c.getValue().getTotal())));
    }

    private void cargarDatos() {
        try {
            List<Expense> lista = repo.listarTodas();
            datos.setAll(lista);
            lblConteo.setText(lista.size() + " gastos");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void nuevoGasto() {
        abrirDialog(null);
    }

    @FXML private void editarGasto() {
        Expense sel = tablaGastos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un gasto para editar."); return; }
        abrirDialog(sel);
    }

    @FXML private void eliminarGasto() {
        Expense sel = tablaGastos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un gasto para eliminar."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la factura de " + sel.getProveedor() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    repo.eliminar(sel.getId());
                    cargarDatos();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    @FXML private void filtrar()       { cargarDatos(); }
    @FXML private void limpiarFiltros() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        cbCategoria.setValue("Todas las categorías");
        txtBuscar.clear();
        cargarDatos();
    }
    @FXML private void exportarExcel() { mostrarAviso("Exportación a Excel próximamente."); }

    private void abrirDialog(Expense gasto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GastoDialog.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(gasto == null ? "Nuevo gasto" : "Editar gasto");
            dialog.setScene(new Scene(loader.load()));

            GastoDialogController ctrl = loader.getController();
            ctrl.setDialog(dialog, gasto);
            ctrl.setOnGuardado(this::cargarDatos);
            dialog.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarAviso(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}