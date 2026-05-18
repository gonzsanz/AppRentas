package com.apprenta.renta.model;

import java.math.BigDecimal;

public record QuarterTotal(
        BigDecimal totalAmount,
        BigDecimal baseAmount,
        BigDecimal ivaAmount
) { }
