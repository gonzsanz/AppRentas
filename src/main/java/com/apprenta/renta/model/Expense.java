package com.apprenta.renta.model;

import java.time.LocalDate;

public class Expense {

    private int id;
    private LocalDate fecha;
    private String numFactura;
    private String proveedor;
    private String nif;
    private String naturaleza;
    private double base;
    private double porcentajeIva;
    private double cuotaIva;
    private double total;

    public Expense() {
        this.porcentajeIva = 21.0;
    }

    public Expense(LocalDate fecha, String numFactura, String proveedor,
                           String nif, String naturaleza, double base, double porcentajeIva) {
        this.fecha = fecha;
        this.numFactura = numFactura;
        this.proveedor = proveedor;
        this.nif = nif;
        this.naturaleza = naturaleza;
        this.porcentajeIva = porcentajeIva;
        setBase(base);
    }

    public void calcular() {
        this.cuotaIva = base * (porcentajeIva / 100.0);
        this.total = base + cuotaIva;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getNumFactura() { return numFactura; }
    public void setNumFactura(String numFactura) { this.numFactura = numFactura; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getNaturaleza() { return naturaleza; }
    public void setNaturaleza(String naturaleza) { this.naturaleza = naturaleza; }

    public double getBase() { return base; }
    public void setBase(double base) { this.base = base; calcular(); }

    public double getPorcentajeIva() { return porcentajeIva; }
    public void setPorcentajeIva(double porcentajeIva) { this.porcentajeIva = porcentajeIva; calcular(); }

    public double getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(double cuotaIva) { this.cuotaIva = cuotaIva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

