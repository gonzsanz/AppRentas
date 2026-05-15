package com.apprenta.renta.repository;

import com.apprenta.renta.model.Expense;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    private final Connection conn;

    public ExpenseRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void guardar(Expense f) throws SQLException {
        String sql = "INSERT INTO factura_recibida (fecha, num_factura, proveedor, nif, naturaleza, base, porcentaje_iva, cuota_iva, total) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getFecha().toString());
            ps.setString(2, f.getNumFactura());
            ps.setString(3, f.getProveedor());
            ps.setString(4, f.getNif());
            ps.setString(5, f.getNaturaleza());
            ps.setDouble(6, f.getBase());
            ps.setDouble(7, f.getPorcentajeIva());
            ps.setDouble(8, f.getCuotaIva());
            ps.setDouble(9, f.getTotal());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) f.setId(keys.getInt(1));
        }
    }

    public void actualizar(Expense f) throws SQLException {
        String sql = "UPDATE factura_recibida SET fecha=?, num_factura=?, proveedor=?, nif=?, naturaleza=?, base=?, porcentaje_iva=?, cuota_iva=?, total=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFecha().toString());
            ps.setString(2, f.getNumFactura());
            ps.setString(3, f.getProveedor());
            ps.setString(4, f.getNif());
            ps.setString(5, f.getNaturaleza());
            ps.setDouble(6, f.getBase());
            ps.setDouble(7, f.getPorcentajeIva());
            ps.setDouble(8, f.getCuotaIva());
            ps.setDouble(9, f.getTotal());
            ps.setInt(10, f.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM factura_recibida WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Expense> listarPorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        List<Expense> lista = new ArrayList<>();
        String sql = "SELECT * FROM factura_recibida WHERE fecha BETWEEN ? AND ? ORDER BY fecha";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Expense> listarTodas() throws SQLException {
        List<Expense> lista = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM factura_recibida ORDER BY fecha DESC")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Expense mapear(ResultSet rs) throws SQLException {
        Expense f = new Expense();
        f.setId(rs.getInt("id"));
        f.setFecha(LocalDate.parse(rs.getString("fecha")));
        f.setNumFactura(rs.getString("num_factura"));
        f.setProveedor(rs.getString("proveedor"));
        f.setNif(rs.getString("nif"));
        f.setNaturaleza(rs.getString("naturaleza"));
        f.setPorcentajeIva(rs.getDouble("porcentaje_iva"));
        f.setBase(rs.getDouble("base"));
        f.setCuotaIva(rs.getDouble("cuota_iva"));
        f.setTotal(rs.getDouble("total"));
        return f;
    }

    public double sumarBasePorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        String sql = "SELECT COALESCE(SUM(base),0) FROM factura_recibida WHERE fecha BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            return ps.executeQuery().getDouble(1);
        }
    }

    public double sumarCuotaIvaPorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        String sql = "SELECT COALESCE(SUM(cuota_iva),0) FROM factura_recibida WHERE fecha BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            return ps.executeQuery().getDouble(1);
        }
    }
}