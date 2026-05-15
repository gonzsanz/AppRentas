package com.apprenta.renta.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Income(
        LocalDate fecha,
        Integer numFactura,
        BigDecimal total,
        boolean cerrado
) {
}
