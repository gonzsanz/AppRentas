package com.apprenta.renta.repository;

import com.apprenta.renta.model.OtherExpense;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OtherExpenseRepository {

    private final Connection conn;

    public OtherExpenseRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public void guardar(OtherExpense g) throws SQLException {
        String sql = "INSERT INTO otro_gasto (fecha, mes, anio, nombre, concepto, importe) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getFecha().toString());
            ps.setString(2, g.getMes());
            ps.setInt(3, g.getAnio());
            ps.setString(4, g.getNombre());
            ps.setString(5, g.getConcepto());
            ps.setDouble(6, g.getImporte());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) g.setId(keys.getInt(1));
        }
    }

    public void actualizar(OtherExpense g) throws SQLException {
        String sql = "UPDATE otro_gasto SET fecha=?, mes=?, anio=?, nombre=?, concepto=?, importe=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getFecha().toString());
            ps.setString(2, g.getMes());
            ps.setInt(3, g.getAnio());
            ps.setString(4, g.getNombre());
            ps.setString(5, g.getConcepto());
            ps.setDouble(6, g.getImporte());
            ps.setInt(7, g.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM otro_gasto WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<OtherExpense> listarPorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        List<OtherExpense> lista = new ArrayList<>();
        String sql = "SELECT * FROM otro_gasto WHERE fecha BETWEEN ? AND ? ORDER BY fecha";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<OtherExpense> listarTodos() throws SQLException {
        List<OtherExpense> lista = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM otro_gasto ORDER BY fecha DESC")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public double sumarPorTrimestre(int anio, int trimestre) throws SQLException {
        int mesInicio = (trimestre - 1) * 3 + 1;
        String desde = LocalDate.of(anio, mesInicio, 1).toString();
        String hasta = LocalDate.of(anio, mesInicio + 2, 1)
                .withDayOfMonth(LocalDate.of(anio, mesInicio + 2, 1).lengthOfMonth()).toString();
        String sql = "SELECT COALESCE(SUM(importe),0) FROM otro_gasto WHERE fecha BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            return ps.executeQuery().getDouble(1);
        }
    }

    private OtherExpense mapear(ResultSet rs) throws SQLException {
        OtherExpense g = new OtherExpense();
        g.setId(rs.getInt("id"));
        g.setFecha(LocalDate.parse(rs.getString("fecha")));
        g.setMes(rs.getString("mes"));
        g.setAnio(rs.getInt("anio"));
        g.setNombre(rs.getString("nombre"));
        g.setConcepto(rs.getString("concepto"));
        g.setImporte(rs.getDouble("importe"));
        return g;
    }
}
