package com.apprenta.renta.controller;

import com.apprenta.renta.model.Income;
import com.apprenta.renta.model.db.IncomeDAO;
import com.apprenta.renta.service.IncomeService;
import com.apprenta.renta.service.IncomeServiceImpl;
import com.apprenta.renta.repository.IncomeRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class IngresoDialogController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtNumFactura;
    @FXML private TextField txtTotal;
    @FXML private CheckBox chkCerrado;
    @FXML private Label lblBaseCalc;
    @FXML private Label lblIvaCalc;

    private Stage dialog;
    private IncomeDAO ingresoEditar;
    private Runnable onGuardado;

    private final IncomeService service = new IncomeServiceImpl(new IncomeRepository());
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpFecha.setValue(LocalDate.now());
        // Recalcular en tiempo real al cambiar el total
        txtTotal.textProperty().addListener((obs, old, val) -> recalcular());
        chkCerrado.selectedProperty().addListener((obs, old, val) -> recalcular());
    }

    public void setDialog(Stage dialog, IncomeDAO ingreso) {
        this.dialog = dialog;
        this.ingresoEditar = ingreso;
        if (ingreso != null) {
            lblTitulo.setText("Editar ingreso");
            dpFecha.setValue(ingreso.fecha());
            txtNumFactura.setText(ingreso.numFactura() != null ? ingreso.numFactura().toString() : "");
            txtTotal.setText(ingreso.total().toPlainString());
            chkCerrado.setSelected(ingreso.cerrado());
        }
    }

    public void setOnGuardado(Runnable callback) {
        this.onGuardado = callback;
    }

    private void recalcular() {
        try {
            BigDecimal total = new BigDecimal(txtTotal.getText().replace(",", "."));
            if (chkCerrado.isSelected() || total.signum() <= 0) {
                lblBaseCalc.setText(fmt.format(0));
                lblIvaCalc.setText(fmt.format(0));
            } else {
                BigDecimal base = total.divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP);
                BigDecimal iva  = total.subtract(base);
                lblBaseCalc.setText(fmt.format(base));
                lblIvaCalc.setText(fmt.format(iva));
            }
        } catch (NumberFormatException ignored) {
            lblBaseCalc.setText("-");
            lblIvaCalc.setText("-");
        }
    }

    @FXML private void guardar() {
        if (dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria.");
            return;
        }
        if (txtTotal.getText().isBlank()) {
            mostrarError("El total es obligatorio.");
            return;
        }
        try {
            BigDecimal total = new BigDecimal(txtTotal.getText().replace(",", "."));
            Integer numFactura = txtNumFactura.getText().isBlank() ? null
                    : Integer.parseInt(txtNumFactura.getText());

            Income income = new Income(dpFecha.getValue(), numFactura, total, chkCerrado.isSelected());
            service.createIncome(income);

            if (onGuardado != null) onGuardado.run();
            dialog.close();
        } catch (NumberFormatException e) {
            mostrarError("El total debe ser un número válido.");
        }
    }

    @FXML private void cancelar() {
        dialog.close();
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}