package com.apprenta.renta.service;

import com.apprenta.renta.model.Income;
import java.math.BigDecimal;

public interface IncomeService {
    void createIncome(final Income income);
    void closeInvoice(final int id);
    BigDecimal calculateMonthlyTotal(final int month, final int year);
}