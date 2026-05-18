package com.apprenta.renta.service;

import com.apprenta.renta.model.Income;
import com.apprenta.renta.model.dao.IncomeDAO;
import com.apprenta.renta.repository.IncomeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class IncomeServiceImpl implements IncomeService {

    private static final BigDecimal DIVISOR_IVA = new BigDecimal("1.21");
    private final IncomeRepository incomeRepository;

    public IncomeServiceImpl(final IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Override
    public void createIncome(final Income income) {
        BigDecimal baseImponible;
        BigDecimal cuotaIva;

        if (income.cerrado() || income.total().signum() <= 0) {
            baseImponible = BigDecimal.ZERO;
            cuotaIva = BigDecimal.ZERO;
        } else {
            baseImponible = income.total().divide(DIVISOR_IVA, 2, RoundingMode.HALF_UP);
            cuotaIva = income.total().subtract(baseImponible);
        }

        final IncomeDAO nuevoIngreso = IncomeDAO.builder()
                .fecha(income.fecha())
                .numFactura(income.numFactura())
                .total(income.total())
                .cuotaIva(cuotaIva)
                .baseImponible(baseImponible)
                .cerrado(income.cerrado())
                .build();
        try {
            incomeRepository.saveIncome(nuevoIngreso);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<IncomeDAO> listByQuarter(final Integer year, final Integer quarter) throws SQLException {
        return incomeRepository.listByQuarter(year, quarter);
    }

    @Override
    public Integer nextInvoiceNumber(final Integer year) throws SQLException {
        return incomeRepository.nextInvoiceNumber(year);
    }

    @Override
    public Integer saveIncome(final IncomeDAO updatedIncome) throws SQLException {
        return incomeRepository.saveIncome(updatedIncome);
    }

    @Override
    public void updateIncome(IncomeDAO updatedIncome) throws SQLException {
        incomeRepository.updateIncome(updatedIncome);
    }

    @Override
    public void deleteIncome(final Integer id) throws SQLException {
        incomeRepository.deleteIncome(id);
    }


}
