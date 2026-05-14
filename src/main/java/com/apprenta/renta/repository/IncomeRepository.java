package com.apprenta.renta.repository;

import com.apprenta.renta.model.db.IncomeDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IncomeRepository {

    private final Connection conn;
    private static final String INSERT_INGRESO = "INSERT INTO ingreso_diario (fecha, num_factura, total, cuota_iva, base_imponible, cerrado) VALUES (?,?,?,?,?,?)";


    public IncomeRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void saveIncome(final IncomeDAO ingreso) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(INSERT_INGRESO, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ingreso.fecha().toString());
            ps.setObject(2, ingreso.numFactura());
            ps.setBigDecimal(3, ingreso.total());
            ps.setBigDecimal(4, ingreso.cuotaIva());
            ps.setBigDecimal(5, ingreso.baseImponible());
            ps.setInt(6, ingreso.cerrado() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ingreso_diario WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<IncomeDAO> listarTodos() throws SQLException {
        List<IncomeDAO> lista = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ingreso_diario ORDER BY fecha DESC")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<IncomeDAO> listarPorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        List<IncomeDAO> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingreso_diario WHERE fecha BETWEEN ? AND ? ORDER BY fecha";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public double sumarBasePorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        String sql = "SELECT COALESCE(SUM(base_imponible),0) FROM ingreso_diario WHERE fecha BETWEEN ? AND ?";
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
        String sql = "SELECT COALESCE(SUM(cuota_iva),0) FROM ingreso_diario WHERE fecha BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            return ps.executeQuery().getDouble(1);
        }
    }

    private IncomeDAO mapear(ResultSet rs) throws SQLException {
        return IncomeDAO.builder()
                .id(rs.getInt("id"))
                .fecha(LocalDate.parse(rs.getString("fecha")))
                .numFactura(rs.getObject("num_factura") != null ? rs.getInt("num_factura") : null)
                .total(rs.getBigDecimal("total"))
                .cuotaIva(rs.getBigDecimal("cuota_iva"))
                .baseImponible(rs.getBigDecimal("base_imponible"))
                .cerrado(rs.getInt("cerrado") == 1)
                .build();
    }
}