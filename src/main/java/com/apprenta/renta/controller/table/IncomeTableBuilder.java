package com.apprenta.renta.controller.table;

import com.apprenta.renta.controller.handler.IncomePersistenceHandler;
import com.apprenta.renta.model.dao.IncomeDAO;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.function.Consumer;

/**
 * Construye el bloque visual mensual: título, tabla de días y etiqueta de total.
 * <p>
 * Se obtiene una {@link VBox} lista para insertar en el {@code monthContainer} del
 * controlador. La etiqueta de total mensual se entrega al llamador mediante el callback
 * {@code onLabelCreated} para que pueda actualizarla posteriormente.
 */
public final class IncomeTableBuilder {

    private static final String[] MONTH_NAMES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private IncomeTableBuilder() {
    }

    /**
     * Crea el bloque visual completo para un mes.
     *
     * @param month              número del mes (1-12)
     * @param year               año del trimestre
     * @param rows               filas de la tabla (un elemento por día del mes)
     * @param persistenceHandler manejador de persistencia que se inyecta en las columnas
     * @param numberFormat       formato de moneda para los importes
     * @param onLabelCreated     callback que recibe la etiqueta de total mensual recién creada
     * @return bloque {@link VBox} con título, tabla y total
     */
    public static VBox buildMonthBlock(final int month,
                                       final int year,
                                       final ObservableList<IncomeDAO> rows,
                                       final IncomePersistenceHandler persistenceHandler,
                                       final NumberFormat numberFormat,
                                       final Consumer<Label> onLabelCreated) {

        final Label title = buildTitle(month, year);
        final TableView<IncomeDAO> table = buildTable(rows, persistenceHandler, numberFormat);
        final Label totalLabel = buildTotalLabel();
        onLabelCreated.accept(totalLabel);

        final VBox card = new VBox(10, title, table, totalLabel);
        card.getStyleClass().add("card");
        return card;
    }

    private static TableView<IncomeDAO> buildTable(final ObservableList<IncomeDAO> rows,
                                                   final IncomePersistenceHandler persistenceHandler,
                                                   final NumberFormat numberFormat) {
        final TableView<IncomeDAO> table = new TableView<>(rows);

        table.getColumns().addAll(
                IncomeColumnFactory.closedColumn(persistenceHandler),
                IncomeColumnFactory.dayNumberColumn(),
                IncomeColumnFactory.dayNameColumn(),
                IncomeColumnFactory.invoiceNumberColumn(),
                IncomeColumnFactory.totalAmountColumn(persistenceHandler),
                IncomeColumnFactory.baseAmountColumn(numberFormat),
                IncomeColumnFactory.ivaAmountColumn(numberFormat)
        );

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setFixedCellSize(28);
        table.setPrefHeight(table.getFixedCellSize() * rows.size() + 30);
        table.getStyleClass().add("data-table");

        attachClickToEdit(table);
        attachRowFactory(table);
        return table;
    }

    /**
     * Abre la edición en la columna TOTAL (índice 4) al hacer clic en una fila no cerrada.
     */
    private static void attachClickToEdit(final TableView<IncomeDAO> table) {
        table.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            final TableView.TableViewSelectionModel<IncomeDAO> sm = table.getSelectionModel();
            if (sm.getSelectedCells().isEmpty()) return;
            final javafx.scene.control.TablePosition<IncomeDAO, ?> pos = sm.getSelectedCells().getFirst();
            final IncomeDAO item = table.getItems().get(pos.getRow());
            if (item == null || item.cerrado()) return;
            if (pos.getColumn() == 4 && table.getEditingCell() == null) {
                table.edit(pos.getRow(), pos.getTableColumn());
            }
        });
    }

    /**
     * Aplica fondo gris a las filas cerradas.
     */
    private static void attachRowFactory(final TableView<IncomeDAO> table) {
        table.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(final IncomeDAO item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.cerrado()) {
                    setStyle("-fx-background-color: #e5e7eb; -fx-opacity: 0.6;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private static Label buildTitle(final int month, final int year) {
        final Label title = new Label(MONTH_NAMES[month - 1].toUpperCase() + " " + year);
        title.getStyleClass().add("card-title");
        return title;
    }

    private static Label buildTotalLabel() {
        final Label label = new Label();
        label.getStyleClass().add("label-bold");
        return label;
    }
}