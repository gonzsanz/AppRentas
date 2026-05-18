package com.apprenta.renta.controller.calculator;

import com.apprenta.renta.model.MonthTotal;
import com.apprenta.renta.model.QuarterTotal;
import com.apprenta.renta.model.dao.IncomeDAO;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcula los totales del trimestre (total, base imponible, cuota IVA)
 * y el total individual por cada mes.
 * No tiene estado propio: todos los métodos son estáticos y puros.
 */
public final class QuarterTotalsCalculator {

    private QuarterTotalsCalculator() {}


    /**
     * Suma total, base imponible e IVA de todas las filas del trimestre.
     *
     * @param rowsByMonth lista con una {@link ObservableList} por mes (3 elementos)
     * @return totales agregados del trimestre
     */
    public static QuarterTotal calculate(final List<ObservableList<IncomeDAO>> rowsByMonth) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal base  = BigDecimal.ZERO;
        BigDecimal iva   = BigDecimal.ZERO;

        for (final ObservableList<IncomeDAO> rows : rowsByMonth) {
            for (final IncomeDAO row : rows) {
                total = total.add(orZero(row.total()));
                base  = base.add(orZero(row.baseImponible()));
                iva   = iva.add(orZero(row.cuotaIva()));
            }
        }
        return new QuarterTotal(total, base, iva);
    }

    /**
     * Devuelve el total de cada mes individualmente.
     *
     * @param rowsByMonth lista con una {@link ObservableList} por mes
     * @param startMonth  número del primer mes del trimestre (1-12)
     * @return lista de {@link MonthTotal}, uno por mes
     */
    public static List<MonthTotal> byMonth(final List<ObservableList<IncomeDAO>> rowsByMonth,
                                           final int startMonth) {
        final List<MonthTotal> result = new ArrayList<>();
        for (int i = 0; i < rowsByMonth.size(); i++) {
            final BigDecimal monthTotal = rowsByMonth.get(i).stream()
                    .map(row -> orZero(row.total()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(new MonthTotal(startMonth + i, monthTotal));
        }
        return result;
    }

    private static BigDecimal orZero(final BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}