package com.apprenta.renta.controller.table;

import com.apprenta.renta.controller.handler.IncomeKeyboardHandler;
import com.apprenta.renta.controller.handler.IncomePersistenceHandler;
import com.apprenta.renta.model.dao.IncomeDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Factoría estática que construye cada columna de la tabla de ingresos.
 * Cada método devuelve una columna completamente configurada (ancho, orden,
 * cellValueFactory, cellFactory y, si aplica, onEditCommit).
 */
public final class IncomeColumnFactory {

    private IncomeColumnFactory() {
    }

    public static TableColumn<IncomeDAO, Boolean> closedColumn(
            final IncomePersistenceHandler persistenceHandler) {

        final TableColumn<IncomeDAO, Boolean> column = new TableColumn<>("CERRADO");
        column.setPrefWidth(70);
        column.setSortable(false);
        column.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().cerrado()));
        column.setCellFactory(col -> new ClosedCheckBoxCell(persistenceHandler));
        return column;
    }

    public static TableColumn<IncomeDAO, String> dayNumberColumn() {
        final TableColumn<IncomeDAO, String> column = new TableColumn<>("#");
        column.setPrefWidth(40);
        column.setSortable(false);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().fecha().getDayOfMonth())));
        return column;
    }

    public static TableColumn<IncomeDAO, String> dayNameColumn() {
        final TableColumn<IncomeDAO, String> column = new TableColumn<>("DÍA");
        column.setPrefWidth(130);
        column.setSortable(false);
        column.setCellValueFactory(c -> new SimpleStringProperty(formatDayName(c.getValue().fecha())));
        column.setCellFactory(col -> new DayNameCell());
        return column;
    }

    public static TableColumn<IncomeDAO, String> invoiceNumberColumn() {
        final TableColumn<IncomeDAO, String> column = new TableColumn<>("Nº FACTURA");
        column.setPrefWidth(90);
        column.setSortable(false);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(c -> {
            final Integer number = c.getValue().numFactura();
            return new SimpleStringProperty(number != null ? String.valueOf(number) : "");
        });
        return column;
    }

    public static TableColumn<IncomeDAO, String> totalAmountColumn(
            final IncomePersistenceHandler persistenceHandler) {

        final TableColumn<IncomeDAO, String> column = new TableColumn<>("TOTAL (€)");
        column.setPrefWidth(110);
        column.setSortable(false);
        column.setCellValueFactory(c -> new SimpleStringProperty(formatTotal(c.getValue())));
        column.setCellFactory(col -> new EditableTotalCell(column));
        column.setOnEditCommit(event -> {
            if (event.getRowValue() != null) {
                persistenceHandler.saveTotal(event.getRowValue(), event.getNewValue());
            }
        });
        return column;
    }

    public static TableColumn<IncomeDAO, String> baseAmountColumn(final NumberFormat fmt) {
        final TableColumn<IncomeDAO, String> column = new TableColumn<>("BASE IMPONIBLE");
        column.setPrefWidth(130);
        column.setSortable(false);
        column.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue(),
                c.getValue().baseImponible(), fmt)));
        return column;
    }

    public static TableColumn<IncomeDAO, String> ivaAmountColumn(final NumberFormat fmt) {
        final TableColumn<IncomeDAO, String> column = new TableColumn<>("IVA (21%)");
        column.setPrefWidth(110);
        column.setSortable(false);
        column.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue(),
                c.getValue().cuotaIva(), fmt)));
        return column;
    }

    private static String formatDayName(final LocalDate date) {
        final String name = date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                .toUpperCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String formatTotal(final IncomeDAO row) {
        final BigDecimal total = row.total();
        if (row.id() != null && total != null) {
            return total.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        return (total != null && total.compareTo(BigDecimal.ZERO) > 0)
                ? total.toPlainString()
                : "";
    }

    private static String formatAmount(final IncomeDAO row,
                                       final BigDecimal amount,
                                       final NumberFormat fmt) {
        if (row.id() != null && amount != null) {
            return fmt.format(amount);
        }
        return (amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                ? fmt.format(amount)
                : "";
    }

    /**
     * Celda con checkbox para la columna CERRADO.
     */
    private static class ClosedCheckBoxCell extends TableCell<IncomeDAO, Boolean> {

        private final CheckBox checkBox = new CheckBox();
        private final IncomePersistenceHandler persistenceHandler;

        ClosedCheckBoxCell(final IncomePersistenceHandler persistenceHandler) {
            this.persistenceHandler = persistenceHandler;
            checkBox.setOnAction(e -> {
                final IncomeDAO row = getTableRow().getItem();
                if (row == null) return;
                persistenceHandler.toggleClosed(row, checkBox.isSelected());
            });
        }

        @Override
        protected void updateItem(final Boolean item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                checkBox.setSelected(item);
                checkBox.setDisable(false);
                setAlignment(Pos.CENTER);
                setGraphic(checkBox);
            }
        }
    }

    /**
     * Celda que colorea el nombre del día de forma diferente para fines de semana.
     */
    private static class DayNameCell extends TableCell<IncomeDAO, String> {

        @Override
        protected void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item);
            if (! empty && getTableRow() != null && getTableRow().getItem() != null) {
                final DayOfWeek dayOfWeek = getTableRow().getItem().fecha().getDayOfWeek();
                setStyle((dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
                        ? "-fx-text-fill: #9ca3af; -fx-font-style: italic;"
                        : "");
            }
        }
    }

    /**
     * Celda editable para la columna TOTAL.
     * Bloquea la edición en filas cerradas.
     * Delega la navegación por teclado en {@link IncomeKeyboardHandler}.
     */
    private static class EditableTotalCell extends TextFieldTableCell<IncomeDAO, String> {

        private final TableColumn<IncomeDAO, String> column;

        EditableTotalCell(final TableColumn<IncomeDAO, String> column) {
            super(new DefaultStringConverter());
            this.column = column;
        }

        @Override
        public void startEdit() {
            final IncomeDAO row = getTableRow().getItem();
            if (row != null && row.cerrado()) return;
            super.startEdit();
            if (! isEditing()) return;
            IncomeKeyboardHandler.attach(this, column);
        }

        @Override
        public void cancelEdit() {
            // Comportamiento estándar: cancelar no guarda.
            super.cancelEdit();
        }

        @Override
        public void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
            }
        }
    }
}