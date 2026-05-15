package com.apprenta.renta.controller;

import com.apprenta.renta.model.QuarterlySummary;
import com.apprenta.renta.repository.ExpenseRepository;
import com.apprenta.renta.repository.IncomeRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class Modelo303Controller implements Initializable {

    @FXML private Label lblTrimestre;
    @FXML private Label lblVentasBase;
    @FXML private Label lblVentasIva;
    @FXML private Label lblComprasBase;
    @FXML private Label lblComprasIva;
    @FXML private Label lblResultado;

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

            QuarterlySummary r = new QuarterlySummary(trimestre, anio);
            r.setVentasBase(incomeRepo.calculateBaseByQuarter(anio, trimestre));
            r.setVentasCuotaIva(incomeRepo.calculateIvaQuotaByQuarter(anio, trimestre));
            r.setComprasBase(expenseRepo.sumarBasePorTrimestre(anio, trimestre));
            r.setComprasCuotaIva(expenseRepo.sumarCuotaIvaPorTrimestre(anio, trimestre));

            String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
            int mi = (trimestre - 1) * 3;
            lblTrimestre.setText(trimestre + "º Trimestre " + anio + " (" + meses[mi] + " - " + meses[mi + 2] + ")");

            lblVentasBase.setText(fmt.format(r.getVentasBase()));
            lblVentasIva.setText(fmt.format(r.getVentasCuotaIva()));
            lblComprasBase.setText(fmt.format(r.getComprasBase()));
            lblComprasIva.setText(fmt.format(r.getComprasCuotaIva()));
            lblResultado.setText(fmt.format(r.getIvaAPagar()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void verDetalle() { /* TODO */ }
    @FXML private void exportarExcel() { /* TODO */ }
}