package com.apprenta.renta.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
@Builder(toBuilder = true)
public record IncomeDTO(
        LocalDate fecha,
        Integer numFactura,
        BigDecimal total,
        BigDecimal cuotaIva,
        BigDecimal baseImponible,
        boolean cerrado
) {
}
