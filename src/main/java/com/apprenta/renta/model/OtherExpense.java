package com.apprenta.renta.model;

import java.time.LocalDate;

public class OtherExpense {

    private int id;
    private LocalDate fecha;
    private String mes;
    private int anio;
    private String nombre;
    private String concepto;
    private double importe;

    public OtherExpense() {}

    public OtherExpense(LocalDate fecha, String mes, int anio, String nombre, String concepto, double importe) {
        this.fecha = fecha;
        this.mes = mes;
        this.anio = anio;
        this.nombre = nombre;
        this.concepto = concepto;
        this.importe = importe;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }
}