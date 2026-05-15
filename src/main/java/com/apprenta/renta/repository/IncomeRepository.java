package com.apprenta.renta.repository;

import com.apprenta.renta.model.dao.IncomeDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IncomeRepository {

    private final Connection conn;

    private static final String INSERT_INCOME = "INSERT INTO ingreso_diario (fecha, num_factura, total, cuota_iva, base_imponible, cerrado) VALUES (?,?,?,?,?,?)";
    private static final String DELETE_INCOME = "DELETE FROM ingreso_diario WHERE id=?";
    private static final String SELECT_INCOMES = "SELECT * FROM ingreso_diario ORDER BY fecha DESC";
    private static final String SELECT_INCOMES_BY_QUARTER = "SELECT * FROM ingreso_diario WHERE fecha BETWEEN ? AND ? ORDER BY fecha";
    private static final String SELECT_BASE_BY_QUARTER = "SELECT COALESCE(SUM(base_imponible),0) FROM ingreso_diario WHERE fecha BETWEEN ? AND ?";
    private static final String SELECT_QUOTA_IVA_BY_QUARTER = "SELECT COALESCE(SUM(cuota_iva),0) FROM ingreso_diario WHERE fecha BETWEEN ? AND ?";


    public IncomeRepository() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public Integer saveIncome(final IncomeDAO ingreso) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(INSERT_INCOME, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ingreso.fecha().toString());
            ps.setObject(2, ingreso.numFactura());
            ps.setBigDecimal(3, ingreso.total());
            ps.setBigDecimal(4, ingreso.cuotaIva());
            ps.setBigDecimal(5, ingreso.baseImponible());
            ps.setInt(6, ingreso.cerrado() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : null;
            }
        }
    }

    public void deleteIncome(final int id) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(DELETE_INCOME)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<IncomeDAO> listAll() throws SQLException {
        final List<IncomeDAO> lista = new ArrayList<>();
        try (final Statement st = conn.createStatement();
             final ResultSet rs = st.executeQuery(SELECT_INCOMES)) {
            while (rs.next()) lista.add(convert(rs));
        }
        return lista;
    }

    public List<IncomeDAO> listByQuarter(final int year, int quarter) throws SQLException {
        final int starterMonth = (quarter - 1) * 3 + 1;
        final String startDate = LocalDate.of(year, starterMonth, 1).toString();
        final String endDate = LocalDate.of(year, starterMonth + 2, 1)
                .withDayOfMonth(LocalDate.of(year, starterMonth + 2, 1).lengthOfMonth()).toString();
        final List<IncomeDAO> lista = new ArrayList<>();
        try (final PreparedStatement ps = conn.prepareStatement(SELECT_INCOMES_BY_QUARTER)) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(convert(rs));
        }
        return lista;
    }

    public double calculateBaseByQuarter(final int year, final int quarter) throws SQLException {
        final int starterMonth = (quarter - 1) * 3 + 1;
        final String startDate = LocalDate.of(year, starterMonth, 1).toString();
        final String endDate = LocalDate.of(year, starterMonth + 2, 1)
                .withDayOfMonth(LocalDate.of(year, starterMonth + 2, 1).lengthOfMonth()).toString();
        try (final PreparedStatement ps = conn.prepareStatement(SELECT_BASE_BY_QUARTER)) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            return ps.executeQuery().getDouble(1);
        }
    }

    public double calculateIvaQuotaByQuarter(final int year, final int quarter) throws SQLException {
        final int starterMonth = (quarter - 1) * 3 + 1;
        final String startDate = LocalDate.of(year, starterMonth, 1).toString();
        final String endDate = LocalDate.of(year, starterMonth + 2, 1)
                .withDayOfMonth(LocalDate.of(year, starterMonth + 2, 1).lengthOfMonth()).toString();
        try (final PreparedStatement ps = conn.prepareStatement(SELECT_QUOTA_IVA_BY_QUARTER)) {
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            return ps.executeQuery().getDouble(1);
        }
    }

    private IncomeDAO convert(final ResultSet rs) throws SQLException {
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

    public void updateIncome(IncomeDAO ingreso) throws SQLException {
        String sql = "UPDATE ingreso_diario SET num_factura=?, total=?, cuota_iva=?, base_imponible=?, cerrado=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, ingreso.numFactura());
            ps.setBigDecimal(2, ingreso.total());
            ps.setBigDecimal(3, ingreso.cuotaIva());
            ps.setBigDecimal(4, ingreso.baseImponible());
            ps.setInt(5, ingreso.cerrado() ? 1 : 0);
            ps.setInt(6, ingreso.id());
            ps.executeUpdate();
        }
    }

    // Siguiente número de factura del año (el máximo + 1)
    public Integer nextInvoiceNumber(int anio) throws SQLException {
        String sql = """
        SELECT COALESCE(MAX(num_factura), 0) + 1
        FROM ingreso_diario
        WHERE strftime('%Y', fecha) = ?
          AND cerrado = 0
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(anio));
            return ps.executeQuery().getInt(1);
        }
    }
}