package com.apprenta.renta.repository;

import com.apprenta.renta.model.Income;
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

}