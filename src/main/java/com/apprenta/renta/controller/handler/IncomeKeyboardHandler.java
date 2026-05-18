package com.apprenta.renta.controller.handler;

import com.apprenta.renta.model.dao.IncomeDAO;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * Gestiona la navegación por teclado dentro de la columna editable de la tabla de ingresos.
 * Soporta ENTER, TAB (confirmar), flechas arriba/abajo (moverse entre filas no cerradas)
 * y ESCAPE (cancelar).
 */
public final class IncomeKeyboardHandler {

    private IncomeKeyboardHandler() {}

    /**
     * Adjunta los listeners de teclado a la celda en edición.
     * Debe llamarse desde dentro de {@code startEdit()}, tras {@code super.startEdit()}.
     */
    public static void attach(final TextFieldTableCell<IncomeDAO, String> cell,
                              final TableColumn<IncomeDAO, String> column) {
        Platform.runLater(() -> {
            final TextField field = (TextField) cell.getGraphic();
            if (field == null) return;
            field.selectAll();
            field.requestFocus();
            field.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case ENTER, TAB -> {
                        cell.commitEdit(field.getText());
                        event.consume();
                    }
                    case DOWN -> {
                        navigateVertical(cell, column, field.getText(), +1);
                        event.consume();
                    }
                    case UP -> {
                        navigateVertical(cell, column, field.getText(), -1);
                        event.consume();
                    }
                    case ESCAPE -> {
                        cell.cancelEdit();
                        event.consume();
                    }
                    default -> {}
                }
            });
        });
    }

    private static void navigateVertical(final TextFieldTableCell<IncomeDAO, String> cell,
                                         final TableColumn<IncomeDAO, String> column,
                                         final String currentValue,
                                         final int direction) {
        final int currentRow = cell.getTableRow().getIndex();
        final TableView<IncomeDAO> table = cell.getTableView();
        cell.commitEdit(currentValue);
        Platform.runLater(() -> {
            final int target = nextEditableRow(table, currentRow, direction);
            if (target >= 0 && target < table.getItems().size()) {
                table.getSelectionModel().clearAndSelect(target, column);
                table.getFocusModel().focus(target, column);
                table.scrollTo(target);
                table.edit(target, column);
            }
        });
    }

    private static int nextEditableRow(final TableView<IncomeDAO> table,
                                       final int from,
                                       final int step) {
        int i = from + step;
        while (i >= 0 && i < table.getItems().size()
                && table.getItems().get(i).cerrado()) {
            i += step;
        }
        return i;
    }
}