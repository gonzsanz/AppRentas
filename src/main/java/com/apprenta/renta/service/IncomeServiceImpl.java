package com.apprenta.renta.service;

import com.apprenta.renta.model.Income;
import com.apprenta.renta.model.db.IncomeDAO;
import com.apprenta.renta.repository.IncomeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeInvoice(int id) {

    }

    @Override
    public BigDecimal calculateMonthlyTotal(int month, int year) {
        return null;
    }

}
