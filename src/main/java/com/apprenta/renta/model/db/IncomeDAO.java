package com.apprenta.renta.model.db;

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
) { }