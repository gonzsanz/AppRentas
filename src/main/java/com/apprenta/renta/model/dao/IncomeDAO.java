package com.apprenta.renta.model.dao;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder(toBuilder = true)
public record IncomeDAO (
        Integer id,
        LocalDate fecha,
        Integer numFactura,
        BigDecimal total,
        BigDecimal cuotaIva,
        BigDecimal baseImponible,
        boolean cerrado
) {
    public static IncomeDAO emptyFor(final LocalDate date) {
        return IncomeDAO.builder()
                .id(null)
                .fecha(date)
                .numFactura(null)
                .total(BigDecimal.ZERO)
                .baseImponible(BigDecimal.ZERO)
                .cuotaIva(BigDecimal.ZERO)
                .cerrado(false)
                .build();
    }
}