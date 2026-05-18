package com.apprenta.renta.service;

import com.apprenta.renta.model.Income;
import com.apprenta.renta.model.dao.IncomeDAO;

import java.sql.SQLException;
import java.util.List;

public interface IncomeService {
    void createIncome(final Income income);
    List<IncomeDAO> listByQuarter(final Integer year, final Integer quarter) throws SQLException;
    Integer nextInvoiceNumber(final Integer year) throws SQLException;
    Integer saveIncome(final IncomeDAO updatedIncome) throws SQLException;
    void updateIncome(final IncomeDAO updatedIncome) throws SQLException;
    void deleteIncome(final Integer incomeId) throws SQLException;
}