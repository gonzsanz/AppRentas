package com.apprenta.renta.controller;

import com.apprenta.renta.model.Expense;
import com.apprenta.renta.repository.ExpenseRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class GastoDialogController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtNumFactura;
    @FXML private TextField txtProveedor;
    @FXML private TextField txtNif;
    @FXML private ComboBox<String> cbNaturaleza;
    @FXML private TextField txtBase;
    @FXML private ComboBox<String> cbPorcentajeIva;
    @FXML private Label lblCuotaCalc;
    @FXML private Label lblTotalCalc;

    private Stage dialog;
    private Expense gastoEditar;
    private Runnable onGuardado;

    private final ExpenseRepository repo = new ExpenseRepository();
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpFecha.setValue(LocalDate.now());
        cbNaturaleza.getItems().addAll(
                "EXISTENCIAS", "FAC. LUZ", "FAC. TELEFONO",
                "REPARACION Y CONSERVACION", "OTROS");
        cbPorcentajeIva.getItems().addAll("21", "10", "4", "0");
        cbPorcentajeIva.setValue("21");

        txtBase.textProperty().addListener((obs, o, v) -> recalcular());
        cbPorcentajeIva.valueProperty().addListener((obs, o, v) -> recalcular());
    }

    public void setDialog(Stage dialog, Expense gasto) {
        this.dialog = dialog;
        this.gastoEditar = gasto;
        if (gasto != null) {
            lblTitulo.setText("Editar gasto");
            dpFecha.setValue(gasto.getFecha());
            txtNumFactura.setText(gasto.getNumFactura());
            txtProveedor.setText(gasto.getProveedor());
            txtNif.setText(gasto.getNif() != null ? gasto.getNif() : "");
            cbNaturaleza.setValue(gasto.getNaturaleza());
            txtBase.setText(String.valueOf(gasto.getBase()));
            cbPorcentajeIva.setValue(String.valueOf((int) gasto.getPorcentajeIva()));
        }
    }

    public void setOnGuardado(Runnable callback) {
        this.onGuardado = callback;
    }

    private void recalcular() {
        try {
            double base = Double.parseDouble(txtBase.getText().replace(",", "."));
            double pct  = Double.parseDouble(cbPorcentajeIva.getValue());
            double cuota = base * pct / 100.0;
            double total = base + cuota;
            lblCuotaCalc.setText(fmt.format(cuota));
            lblTotalCalc.setText(fmt.format(total));
        } catch (NumberFormatException ignored) {
            lblCuotaCalc.setText("-");
            lblTotalCalc.setText("-");
        }
    }

    @FXML private void guardar() {
        if (dpFecha.getValue() == null || txtProveedor.getText().isBlank()
                || txtBase.getText().isBlank() || cbNaturaleza.getValue() == null) {
            mostrarError("Fecha, proveedor, categoría y base son obligatorios.");
            return;
        }
        try {
            double base = Double.parseDouble(txtBase.getText().replace(",", "."));
            double pct  = Double.parseDouble(cbPorcentajeIva.getValue());

            Expense gasto = gastoEditar != null ? gastoEditar : new Expense();
            gasto.setFecha(dpFecha.getValue());
            gasto.setNumFactura(txtNumFactura.getText());
            gasto.setProveedor(txtProveedor.getText());
            gasto.setNif(txtNif.getText().isBlank() ? null : txtNif.getText());
            gasto.setNaturaleza(cbNaturaleza.getValue());
            gasto.setPorcentajeIva(pct);
            gasto.setBase(base);

            if (gastoEditar != null) repo.actualizar(gasto);
            else repo.guardar(gasto);

            if (onGuardado != null) onGuardado.run();
            dialog.close();
        } catch (Exception e) {
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML private void cancelar() { dialog.close(); }

    private void mostrarError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}