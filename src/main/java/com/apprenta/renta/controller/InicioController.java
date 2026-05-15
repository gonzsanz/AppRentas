package com.apprenta.renta.controller;

import com.apprenta.renta.model.QuarterlySummary;
import com.apprenta.renta.repository.ExpenseRepository;
import com.apprenta.renta.repository.IncomeRepository;
import com.apprenta.renta.repository.OtherExpenseRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class InicioController implements Initializable {

    @FXML private Label lblTrimestre;
    @FXML private Label lblIvaRepercutido;
    @FXML private Label lblIvaSoportado;
    @FXML private Label lblResultadoIva;
    @FXML private Label lblIngresos;
    @FXML private Label lblGastos;
    @FXML private Label lblBeneficio;
    @FXML private Label lblPagoFraccionado;

    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private int trimestre;
    private int anio;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Trimestre actual
        int mes = LocalDate.now().getMonthValue();
        trimestre = (mes - 1) / 3 + 1;
        anio = LocalDate.now().getYear();
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            var incomeRepo  = new IncomeRepository();
            var expenseRepo = new ExpenseRepository();
            var otherRepo   = new OtherExpenseRepository();

            QuarterlySummary resumen = new QuarterlySummary(trimestre, anio);
            resumen.setVentasBase(incomeRepo.calculateBaseByQuarter(anio, trimestre));
            resumen.setVentasCuotaIva(incomeRepo.calculateIvaQuotaByQuarter(anio, trimestre));
            resumen.setComprasBase(expenseRepo.sumarBasePorTrimestre(anio, trimestre));
            resumen.setComprasCuotaIva(expenseRepo.sumarCuotaIvaPorTrimestre(anio, trimestre));
            resumen.setOtrosGastos(otherRepo.sumarPorTrimestre(anio, trimestre));

            String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
            int mesInicio = (trimestre - 1) * 3;
            lblTrimestre.setText(trimestre + "º Trimestre " + anio +
                    " (" + meses[mesInicio] + " - " + meses[mesInicio + 2] + ")");

            lblIvaRepercutido.setText(fmt.format(resumen.getVentasCuotaIva()));
            lblIvaSoportado.setText(fmt.format(resumen.getComprasCuotaIva()));
            lblResultadoIva.setText(fmt.format(resumen.getIvaAPagar()));

            lblIngresos.setText(fmt.format(resumen.getVentasBase()));
            lblGastos.setText("-" + fmt.format(resumen.getTotalGastos()));
            lblBeneficio.setText(fmt.format(resumen.getNetoTrimestral()));
            lblPagoFraccionado.setText(fmt.format(resumen.getNetoTrimestral() * 0.20));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void cambiarTrimestre() {
        trimestre = trimestre < 4 ? trimestre + 1 : 1;
        if (trimestre == 1) anio++;
        cargarDatos();
    }
}