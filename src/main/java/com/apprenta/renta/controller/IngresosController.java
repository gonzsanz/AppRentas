package com.apprenta.renta.controller;

import com.apprenta.renta.model.db.IncomeDAO;
import com.apprenta.renta.repository.IncomeRepository;
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

public class IngresosController implements Initializable {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtBuscar;
    @FXML private TableView<IncomeDAO> tablaIngresos;
    @FXML private TableColumn<IncomeDAO, String> colFecha;
    @FXML private TableColumn<IncomeDAO, String> colNumFactura;
    @FXML private TableColumn<IncomeDAO, String> colTotal;
    @FXML private TableColumn<IncomeDAO, String> colBaseImponible;
    @FXML private TableColumn<IncomeDAO, String> colCuotaIva;
    @FXML private TableColumn<IncomeDAO, String> colCerrado;
    @FXML private Label lblConteo;

    private final IncomeRepository repo = new IncomeRepository();
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private ObservableList<IncomeDAO> datos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        tablaIngresos.setItems(datos);

        // Trimestre actual por defecto
        int mes = LocalDate.now().getMonthValue();
        int tri = (mes - 1) / 3 + 1;
        int mesInicio = (tri - 1) * 3 + 1;
        dpDesde.setValue(LocalDate.of(LocalDate.now().getYear(), mesInicio, 1));
        dpHasta.setValue(LocalDate.of(LocalDate.now().getYear(), mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(LocalDate.now().getYear(), mesInicio + 2, 1).lengthOfMonth()));

        cargarDatos();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fecha().toString()));
        colNumFactura.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().numFactura() != null ? c.getValue().numFactura().toString() : "-"));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(fmt.format(c.getValue().total())));
        colBaseImponible.setCellValueFactory(c -> new SimpleStringProperty(fmt.format(c.getValue().baseImponible())));
        colCuotaIva.setCellValueFactory(c -> new SimpleStringProperty(fmt.format(c.getValue().cuotaIva())));
        colCerrado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cerrado() ? "✓" : ""));
    }

    private void cargarDatos() {
        try {
            List<IncomeDAO> lista = repo.listarTodos();
            datos.setAll(lista);
            lblConteo.setText(lista.size() + " ingresos");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void nuevoIngreso() {
        abrirDialog(null);
    }

    @FXML private void editarIngreso() {
        IncomeDAO seleccionado = tablaIngresos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAviso("Selecciona un ingreso para editar.");
            return;
        }
        abrirDialog(seleccionado);
    }

    @FXML private void eliminarIngreso() {
        IncomeDAO seleccionado = tablaIngresos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAviso("Selecciona un ingreso para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el ingreso del " + seleccionado.fecha() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    repo.eliminar(seleccionado.id());
                    cargarDatos();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void filtrar() {
        // TODO: filtrar por rango de fechas y texto
        cargarDatos();
    }

    @FXML private void limpiarFiltros() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        txtBuscar.clear();
        cargarDatos();
    }

    @FXML private void exportarExcel() {
        // TODO: exportar a Excel
        mostrarAviso("Exportación a Excel próximamente.");
    }

    private void abrirDialog(IncomeDAO ingreso) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/IngresoDialog.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(ingreso == null ? "Nuevo ingreso" : "Editar ingreso");
            dialog.setScene(new Scene(loader.load()));

            IngresoDialogController ctrl = loader.getController();
            ctrl.setDialog(dialog, ingreso);
            ctrl.setOnGuardado(this::cargarDatos);

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAviso(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}