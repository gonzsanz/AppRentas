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

public class Modelo130Controller implements Initializable {

    @FXML private Label lblTrimestre;
    @FXML private Label lblIngresos;
    @FXML private Label lblGastos;
    @FXML private Label lblBeneficio;
    @FXML private Label lblPagoDesc;
    @FXML private Label lblPago;

    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int mes = LocalDate.now().getMonthValue();
        int trimestre = (mes - 1) / 3 + 1;
        int anio = LocalDate.now().getYear();
        cargarDatos(trimestre, anio);
    }

    private void cargarDatos(int trimestre, int anio) {
        try {
            var incomeRepo  = new IncomeRepository();
            var expenseRepo = new ExpenseRepository();
            var otherRepo   = new OtherExpenseRepository();

            QuarterlySummary r = new QuarterlySummary(trimestre, anio);
            r.setVentasBase(incomeRepo.calculateBaseByQuarter(anio, trimestre));
            r.setComprasBase(expenseRepo.sumarBasePorTrimestre(anio, trimestre));
            r.setOtrosGastos(otherRepo.sumarPorTrimestre(anio, trimestre));

            String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
            int mi = (trimestre - 1) * 3;
            lblTrimestre.setText(trimestre + "º Trimestre " + anio + " (" + meses[mi] + " - " + meses[mi + 2] + ")");

            double beneficio = r.getNetoTrimestral();
            double pago = beneficio > 0 ? beneficio * 0.20 : 0;

            lblIngresos.setText(fmt.format(r.getVentasBase()));
            lblGastos.setText("-" + fmt.format(r.getTotalGastos()));
            lblBeneficio.setText(fmt.format(beneficio));
            lblPagoDesc.setText("20% de " + fmt.format(beneficio));
            lblPago.setText(fmt.format(pago));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void verDetalle() { /* TODO */ }
    @FXML private void exportarExcel() { /* TODO */ }
}