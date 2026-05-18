package com.apprenta.renta.model;

import java.math.BigDecimal;

public record MonthTotal(
        Integer month,
        BigDecimal totalAmount
) { }
