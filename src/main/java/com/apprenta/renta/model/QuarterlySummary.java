package com.apprenta.renta.model;

public class QuarterlySummary {

    private int trimestre;
    private int anio;

    // Ventas
    private double ventasBase;
    private double ventasCuotaIva;

    // Gastos facturas recibidas
    private double comprasBase;
    private double comprasCuotaIva;

    // Otros gastos (sin IVA)
    private double otrosGastos;

    public QuarterlySummary(int trimestre, int anio) {
        this.trimestre = trimestre;
        this.anio = anio;
    }

    // IVA Modelo 303
    public double getIvaAPagar() {
        return ventasCuotaIva - comprasCuotaIva;
    }

    // IRPF Modelo 130
    public double getTotalGastos() {
        return comprasBase + otrosGastos;
    }

    public double getNetoTrimestral() {
        return ventasBase - getTotalGastos();
    }

    public String getNombreTrimestre() {
        return anio + "-" + trimestre + "T";
    }

    // Getters y setters
    public int getTrimestre() { return trimestre; }
    public int getAnio() { return anio; }

    public double getVentasBase() { return ventasBase; }
    public void setVentasBase(double ventasBase) { this.ventasBase = ventasBase; }

    public double getVentasCuotaIva() { return ventasCuotaIva; }
    public void setVentasCuotaIva(double ventasCuotaIva) { this.ventasCuotaIva = ventasCuotaIva; }

    public double getComprasBase() { return comprasBase; }
    public void setComprasBase(double comprasBase) { this.comprasBase = comprasBase; }

    public double getComprasCuotaIva() { return comprasCuotaIva; }
    public void setComprasCuotaIva(double comprasCuotaIva) { this.comprasCuotaIva = comprasCuotaIva; }

    public double getOtrosGastos() { return otrosGastos; }
    public void setOtrosGastos(double otrosGastos) { this.otrosGastos = otrosGastos; }
}