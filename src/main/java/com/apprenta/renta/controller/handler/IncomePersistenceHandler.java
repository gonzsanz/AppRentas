package com.apprenta.renta.controller.handler;

import com.apprenta.renta.model.dao.IncomeDAO;
import com.apprenta.renta.service.IncomeService;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Encapsula toda la lógica de persistencia de ingresos: guardar, actualizar, borrar
 * y marcar como cerrado. Al finalizar cualquier operación invoca {@code onChangeCallback}
 * para que el controlador refresque los totales.
 */
public class IncomePersistenceHandler {

    private static final BigDecimal IVA_DIVISOR = new BigDecimal("1.21");

    private final IncomeService incomeService;
    private final List<ObservableList<IncomeDAO>> rowsByMonth;
    private final Map<LocalDate, IncomeDAO> dbData;
    private final Runnable onChangeCallback;

    public IncomePersistenceHandler(final IncomeService incomeService,
                                    final List<ObservableList<IncomeDAO>> rowsByMonth,
                                    final Map<LocalDate, IncomeDAO> dbData,
                                    final Runnable onChangeCallback) {
        this.incomeService = incomeService;
        this.rowsByMonth = rowsByMonth;
        this.dbData = dbData;
        this.onChangeCallback = onChangeCallback;
    }

    /**
     * Procesa el valor introducido por el usuario en la celda TOTAL y persiste el resultado.
     * Si el valor es vacío y la fila aún no tiene ID, no hace nada.
     * Si el valor es vacío y la fila ya existe en BD, la elimina.
     *
     * @param row      fila que se está editando
     * @param rawValue texto tal cual lo tecleó el usuario (puede ser null o vacío)
     */
    public void saveTotal(final IncomeDAO row, final String rawValue) {
        try {
            if (isEmpty(rawValue) && row.id() == null) return;

            final BigDecimal totalAmount = parseTotal(rawValue);

            if (totalAmount.compareTo(BigDecimal.ZERO) == 0
                    && row.id() != null
                    && isEmpty(rawValue)) {
                deleteAndRefresh(row);
                return;
            }

            persist(row, buildUpdatedIncome(row, totalAmount));

        } catch (NumberFormatException e) {
            onChangeCallback.run(); // valor inválido → recarga para deshacer el cambio visual
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Activa o desactiva el estado "cerrado" de una fila.
     * Al cerrar se ponen todos los importes a cero y se elimina el número de factura.
     *
     * @param row      fila a modificar
     * @param isClosed nuevo estado deseado
     */
    public void toggleClosed(final IncomeDAO row, final boolean isClosed) {
        final IncomeDAO updatedIncome = isClosed
                ? row.toBuilder()
                  .cerrado(true)
                  .total(BigDecimal.ZERO)
                  .baseImponible(BigDecimal.ZERO)
                  .cuotaIva(BigDecimal.ZERO)
                  .numFactura(null)
                  .build()
                : row.toBuilder()
                  .cerrado(false)
                  .build();
        persist(row, updatedIncome);
    }

    private IncomeDAO buildUpdatedIncome(final IncomeDAO row, final BigDecimal totalAmount) throws Exception {
        final BigDecimal baseAmount = totalAmount.compareTo(BigDecimal.ZERO) > 0
                ? totalAmount.divide(IVA_DIVISOR, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final BigDecimal ivaAmount = totalAmount.subtract(baseAmount).setScale(2, RoundingMode.HALF_UP);

        final Integer invoiceNumber = row.numFactura() != null
                ? row.numFactura()
                : incomeService.nextInvoiceNumber(row.fecha().getYear());

        return row.toBuilder()
                .total(totalAmount)
                .baseImponible(baseAmount)
                .cuotaIva(ivaAmount)
                .numFactura(invoiceNumber)
                .build();
    }

    private void persist(final IncomeDAO previous, final IncomeDAO updated) {
        try {
            for (final ObservableList<IncomeDAO> rows : rowsByMonth) {
                final int index = rows.indexOf(previous);
                if (index == - 1) continue;

                if (previous.id() == null) {
                    final Integer newId = incomeService.saveIncome(updated);
                    final IncomeDAO withId = updated.toBuilder().id(newId).build();
                    rows.set(index, withId);
                    dbData.put(withId.fecha(), withId);
                } else {
                    incomeService.updateIncome(updated);
                    rows.set(index, updated);
                    dbData.put(updated.fecha(), updated);
                }
                break;
            }
            onChangeCallback.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteAndRefresh(final IncomeDAO row) throws Exception {
        incomeService.deleteIncome(row.id());
        for (final ObservableList<IncomeDAO> rows : rowsByMonth) {
            final int index = rows.indexOf(row);
            if (index != - 1) {
                rows.set(index, IncomeDAO.emptyFor(row.fecha()));
                dbData.remove(row.fecha());
                break;
            }
        }
        onChangeCallback.run();
    }

    private static boolean isEmpty(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private static BigDecimal parseTotal(final String value) {
        return isEmpty(value)
                ? BigDecimal.ZERO
                : new BigDecimal(value.replace(",", ".").trim());
    }
}