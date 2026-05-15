package com.apprenta.renta.controller;

import com.apprenta.renta.model.dao.IncomeDAO;
import com.apprenta.renta.repository.IncomeRepository;
import com.apprenta.renta.service.IncomeService;
import com.apprenta.renta.service.IncomeServiceImpl;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class IngresosController implements Initializable {

    @FXML
    private ComboBox<String> cbQuarterYear;
    @FXML
    private ComboBox<String> cbQuarter;
    @FXML
    private VBox monthContainer;
    @FXML
    private Label totalAmountQuarterLabel;
    @FXML
    private Label baseAmountQuarterLabel;
    @FXML
    private Label ivaAmountQuarterLabel;

    private final IncomeService incomeService;
    private final NumberFormat numberFormat;

    private int quarter;
    private int year;

    private final List<ObservableList<IncomeDAO>> rowsByMonth;
    private final Map<LocalDate, IncomeDAO> dbData;

    private static final String[] MONTHS_NAMES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private final Map<Integer, Label> totalLabelsByMonth;

    public IngresosController() {
        this(new IncomeServiceImpl(new IncomeRepository()));
    }

    public IngresosController(final IncomeService incomeService) {
        this.incomeService = incomeService;
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        this.numberFormat.setMinimumFractionDigits(2); // Fuerza los céntimos ,00
        this.dbData = new HashMap<>();
        this.rowsByMonth = new ArrayList<>();
        this.totalLabelsByMonth = new HashMap<>();
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        final int actualMonth = LocalDate.now().getMonthValue();
        quarter = (actualMonth - 1) / 3 + 1;
        year = LocalDate.now().getYear();

        initializeCombo();
        cbQuarterYear.setValue(String.valueOf(year));
        cbQuarter.setValue(quarter + "T");
        loadQuarter();
    }

    // ── Combo ─────────────────────────────────────────────────────────────────

    private void initializeCombo() {
        final List<String> quarters = new ArrayList<>();
        final List<String> years = new ArrayList<>();
        for (int a = year - 2; a <= year; a++) {
            years.add(String.valueOf(a));
        }
        for (int t = 1; t <= 4; t++) {
            quarters.add(t + "T");
        }
        cbQuarterYear.setItems(FXCollections.observableArrayList(years));
        cbQuarter.setItems(FXCollections.observableArrayList(quarters));
    }

    @FXML
    private void selectYearCombo() {
        final String cbQuarterYearValue = cbQuarterYear.getValue();
        if (cbQuarterYearValue == null) return;
        year = Integer.parseInt(cbQuarterYearValue);
        loadQuarter();
    }

    @FXML
    private void selectQuarterCombo() {
        final String cbQuarterValue = cbQuarter.getValue();
        if (cbQuarterValue == null) return;
        quarter = Integer.parseInt(cbQuarterValue.substring(0, 1));
        loadQuarter();
    }
    // ── Carga ─────────────────────────────────────────────────────────────────

    private void loadQuarter() {
        try {
            dbData.clear();
            incomeService.listByQuarter(year, quarter)
                    .forEach(dao -> dbData.put(dao.fecha(), dao));

            rowsByMonth.clear();
            monthContainer.getChildren().clear();
            totalLabelsByMonth.clear();

            final int starterMonth = (quarter - 1) * 3 + 1;
            for (int i = 0; i < 3; i++) {
                final int month = starterMonth + i;
                final ObservableList<IncomeDAO> rows = FXCollections.observableArrayList();
                final LocalDate firstDay = LocalDate.of(year, month, 1);
                for (int d = 1; d <= firstDay.lengthOfMonth(); d++) {
                    final LocalDate date = LocalDate.of(year, month, d);
                    final IncomeDAO actualIncome = dbData.get(date);
                    rows.add(actualIncome != null ? actualIncome : emptyRow(date));
                }
                rowsByMonth.add(rows);
                monthContainer.getChildren().add(buildMonthlyBlock(month, rows));
            }

            updateTotalAmounts();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── Bloque por mes ────────────────────────────────────────────────────────

    private VBox buildMonthlyBlock(final int month, final ObservableList<IncomeDAO> rows) {
        // Cabecera del mes
        final Label title = getTitleLabel(month);

        // Tabla
        final TableView<IncomeDAO> table = getIncomeTableView(rows);

        final TableColumn<IncomeDAO, Boolean> isCloseColumn = getIsCloseColumn();
        final TableColumn<IncomeDAO, String> dayColumn = getDayColumn();
        final TableColumn<IncomeDAO, String> dateColumn = getDateColumn();
        final TableColumn<IncomeDAO, String> invoiceNumberColumn = getInvoiceNumberColumn();
        final TableColumn<IncomeDAO, String> totalAmountColumn = getTotalAmountColumn();
        final TableColumn<IncomeDAO, String> baseAmountColumn = getBaseAmountColumn();
        final TableColumn<IncomeDAO, String> ivaAmountColumn = getIvaAmountColumn();

        table.getColumns()
                .addAll(
                        isCloseColumn,
                        dayColumn,
                        dateColumn,
                        invoiceNumberColumn,
                        totalAmountColumn,
                        baseAmountColumn,
                        ivaAmountColumn
                );

        // Totales del mes
        final Label monthlyTotalAmountLabel = getMonthlyTotalAmountLabel(month);

        return getCard(title, table, monthlyTotalAmountLabel);
    }

    private Label getMonthlyTotalAmountLabel(final int month) {
        final Label monthlyTotalAmountLabel = new Label();
        monthlyTotalAmountLabel.getStyleClass().add("label-bold");
        totalLabelsByMonth.put(month, monthlyTotalAmountLabel);

        return monthlyTotalAmountLabel;
    }

    private static VBox getCard(final Label title, final TableView<IncomeDAO> table, final Label monthlyTotalAmountLabel) {
        final VBox card = new VBox(10, title, table, monthlyTotalAmountLabel);
        card.getStyleClass().add("card");

        return card;
    }

    private TableColumn<IncomeDAO, Boolean> getIsCloseColumn() {
        final TableColumn<IncomeDAO, Boolean> isCloseColumn = new TableColumn<>("CERRADO");

        isCloseColumn.setPrefWidth(70);
        isCloseColumn.setSortable(false);
        isCloseColumn.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().cerrado()));

        isCloseColumn.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    IncomeDAO row = getTableRow().getItem();
                    if (row != null) {
                        try {
                            toggleCerrado(row, cb.isSelected());
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    cb.setSelected(item);
                    cb.setDisable(false); // el checkbox siempre activo aunque la fila esté deshabilitada
                    setAlignment(Pos.CENTER);
                    setGraphic(cb);
                }
            }
        });

        return isCloseColumn;
    }

    private TableColumn<IncomeDAO, String> getIvaAmountColumn() {
        final TableColumn<IncomeDAO, String> ivaAmountColumn = new TableColumn<>("IVA (21%)");

        ivaAmountColumn.setPrefWidth(110);
        ivaAmountColumn.setSortable(false);

        ivaAmountColumn.setCellValueFactory(c -> {
            final IncomeDAO row = c.getValue();
            final BigDecimal ivaAmount = row.cuotaIva();

            if (row.id() != null && ivaAmount != null) {
                return new SimpleStringProperty(numberFormat.format(ivaAmount));
            }
            return new SimpleStringProperty(
                    (ivaAmount != null && ivaAmount.compareTo(BigDecimal.ZERO) > 0) ? numberFormat.format(ivaAmount) : "");
        });

        return ivaAmountColumn;
    }

    private TableColumn<IncomeDAO, String> getBaseAmountColumn() {
        final TableColumn<IncomeDAO, String> baseAmountColumn = new TableColumn<>("BASE IMPONIBLE");

        baseAmountColumn.setPrefWidth(130);
        baseAmountColumn.setSortable(false);

        baseAmountColumn.setCellValueFactory(c -> {
            final IncomeDAO row = c.getValue();
            BigDecimal baseAmount = row.baseImponible();

            if (row.id() != null && baseAmount != null) {
                return new SimpleStringProperty(numberFormat.format(baseAmount));
            }

            return new SimpleStringProperty(
                    (baseAmount != null && baseAmount.compareTo(BigDecimal.ZERO) > 0) ? numberFormat.format(baseAmount) : "");
        });

        return baseAmountColumn;
    }

    private TableColumn<IncomeDAO, String> getTotalAmountColumn() {
        final TableColumn<IncomeDAO, String> totalAmountColumn = new TableColumn<>("TOTAL (€)");

        totalAmountColumn.setPrefWidth(110);
        totalAmountColumn.setSortable(false);

        totalAmountColumn.setCellValueFactory(c -> {
            final IncomeDAO row = c.getValue();
            final BigDecimal totalAmount = row.total();

            if (row.id() != null && totalAmount != null) {
                return new SimpleStringProperty(totalAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
            }

            return new SimpleStringProperty(
                    (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) ? totalAmount.toPlainString() : "");
        });

        totalAmountColumn.setCellFactory(col -> new TextFieldTableCell<>(new DefaultStringConverter()) {

            @Override
            public void startEdit() {
                final IncomeDAO row = getTableRow().getItem();
                if (row != null && row.cerrado()) return;
                super.startEdit();
                if (!isEditing()) return;

                javafx.application.Platform.runLater(() -> {
                    final TextField textField = (TextField) getGraphic();
                    if (textField == null) return;
                    textField.selectAll();
                    textField.requestFocus();

                    textField.setOnKeyPressed(e -> {
                        switch (e.getCode()) {
                            case ENTER, TAB -> {
                                commitEdit(textField.getText());
                                e.consume();
                            }
                            case DOWN -> {
                                final String value = textField.getText();
                                final int actualRow = getTableRow().getIndex();
                                final TableColumn<IncomeDAO, String> col2 = getTableColumn();
                                final TableView<IncomeDAO> tableView = getTableView();
                                commitEdit(value);
                                javafx.application.Platform.runLater(() -> {
                                    int next = actualRow + 1;
                                    while (next < tableView.getItems().size()
                                            && tableView.getItems().get(next).cerrado()) {
                                        next++;
                                    }
                                    if (next < tableView.getItems().size()) {
                                        tableView.getSelectionModel().clearAndSelect(next, col2);
                                        tableView.getFocusModel().focus(next, col2);
                                        tableView.scrollTo(next);
                                        tableView.edit(next, col2);
                                    }
                                });
                                e.consume();
                            }
                            case UP -> {
                                final String value = textField.getText();
                                final int actualRow = getTableRow().getIndex();
                                final TableColumn<IncomeDAO, String> col2 = getTableColumn();
                                final TableView<IncomeDAO> tableView = getTableView();
                                commitEdit(value);
                                javafx.application.Platform.runLater(() -> {
                                    int previous = actualRow - 1;
                                    while (previous >= 0
                                            && tableView.getItems().get(previous).cerrado()) {
                                        previous--;
                                    }
                                    if (previous >= 0) {
                                        tableView.getSelectionModel().clearAndSelect(previous, col2);
                                        tableView.getFocusModel().focus(previous, col2);
                                        tableView.scrollTo(previous);
                                        tableView.edit(previous, col2);
                                    }
                                });
                                e.consume();
                            }
                            case ESCAPE -> {
                                super.cancelEdit();
                                e.consume();
                            }
                            default -> {}
                        }
                    });
                });
            }

            @Override
            public void cancelEdit() {
                final TextField textField = (TextField) getGraphic();
                if (textField != null) {
                    commitEdit(textField.getText());
                } else {
                    super.cancelEdit();
                }
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
        });

        totalAmountColumn.setOnEditCommit(e -> {
            final IncomeDAO row = e.getRowValue();
            if (row != null) {
                guardarTotal(row, e.getNewValue());
            }
        });

        return totalAmountColumn;
    }

    private static TableColumn<IncomeDAO, String> getInvoiceNumberColumn() {
        final TableColumn<IncomeDAO, String> invoiceNumberColumn = new TableColumn<>("Nº FACTURA");

        invoiceNumberColumn.setPrefWidth(90);
        invoiceNumberColumn.setSortable(false);
        invoiceNumberColumn.setStyle("-fx-alignment: CENTER;");

        invoiceNumberColumn.setCellValueFactory(c -> {
            final Integer invoiceNumber = c.getValue().numFactura();
            return new SimpleStringProperty(invoiceNumber != null ? String.valueOf(invoiceNumber) : "");
        });

        return invoiceNumberColumn;
    }

    private static TableColumn<IncomeDAO, String> getDateColumn() {
        final TableColumn<IncomeDAO, String> dateColumn = new TableColumn<>("DÍA");

        dateColumn.setPrefWidth(130);
        dateColumn.setSortable(false);

        dateColumn.setCellValueFactory(c -> {
            final LocalDate f = c.getValue().fecha();
            String day = f.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase();
            day = Character.toUpperCase(day.charAt(0)) + day.substring(1);
            return new SimpleStringProperty(day);
        });
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                    final DayOfWeek dayOfWeek = getTableRow().getItem().fecha().getDayOfWeek();
                    // Solo colorear fines de semana, lo de cerrado ya lo hace el rowFactory
                    if (dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.SATURDAY) {
                        setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        return dateColumn;
    }

    private static TableColumn<IncomeDAO, String> getDayColumn() {
        final TableColumn<IncomeDAO, String> dayColumn = new TableColumn<>("#");

        dayColumn.setPrefWidth(40);
        dayColumn.setSortable(false);
        dayColumn.setStyle("-fx-alignment: CENTER;");

        dayColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().fecha().getDayOfMonth())));

        return dayColumn;
    }

    private static TableView<IncomeDAO> getIncomeTableView(final ObservableList<IncomeDAO> rows) {
        final TableView<IncomeDAO> table = new TableView<>(rows);

        table.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            TableView.TableViewSelectionModel<IncomeDAO> sm = table.getSelectionModel();
            TablePosition<IncomeDAO, ?> pos = sm.getSelectedCells().isEmpty() ? null : sm.getSelectedCells().get(0);
            if (pos == null) return;
            final IncomeDAO item = table.getItems().get(pos.getRow());
            if (item == null || item.cerrado()) return;
            // Solo abrir si la columna es la de TOTAL (índice 4)
            if (pos.getColumn() == 4 && table.getEditingCell() == null) {
                table.edit(pos.getRow(), pos.getTableColumn());
            }
        });
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(IncomeDAO item, boolean empty) {
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

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setFixedCellSize(28);
        table.setPrefHeight(table.getFixedCellSize() * rows.size() + 30);
        table.getStyleClass().add("data-table");

        return table;
    }

    private Label getTitleLabel(final int mes) {
        final Label title = new Label(MONTHS_NAMES[mes - 1].toUpperCase() + " " + year);
        title.getStyleClass().add("card-title");

        return title;
    }

    // ── Guardado ──────────────────────────────────────────────────────────────

    private void guardarTotal(final IncomeDAO row, final String textValue) {
        try {
            // Celda vacía sin tocar → no hacer nada
            if ((textValue == null || textValue.trim().isEmpty()) && row.id() == null) {
                return;
            }

            BigDecimal total;
            if (textValue == null || textValue.trim().isEmpty()) {
                total = BigDecimal.ZERO;
            } else {
                total = new BigDecimal(textValue.replace(",", ".").trim());
            }

            // Fila existente borrada (se dejó vacía) → eliminar de BD
            if (total.compareTo(BigDecimal.ZERO) == 0 && row.id() != null
                    && (textValue == null || textValue.trim().isEmpty())) {
                incomeService.deleteIncome(row.id());
                for (ObservableList<IncomeDAO> monthRows : rowsByMonth) {
                    final int index = monthRows.indexOf(row);
                    if (index != -1) {
                        monthRows.set(index, emptyRow(row.fecha()));
                        dbData.remove(row.fecha());
                        break;
                    }
                }
                updateTotalAmounts();
                return;
            }

            // A partir de aquí: el usuario escribió algo (0 o positivo)
            final BigDecimal baseAmount = total.compareTo(BigDecimal.ZERO) > 0
                    ? total.divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            final BigDecimal ivaAmount = total.subtract(baseAmount).setScale(2, RoundingMode.HALF_UP);

            final Integer invoiceNumber = row.numFactura() != null
                    ? row.numFactura()
                    : incomeService.nextInvoiceNumber(year);

            final IncomeDAO updatedIncome = row.toBuilder()
                    .total(total)
                    .baseImponible(baseAmount)
                    .cuotaIva(ivaAmount)
                    .numFactura(invoiceNumber)
                    .build();

            persistir(row, updatedIncome);

        } catch (NumberFormatException e) {
            loadQuarter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // En IngresosController.java
    private void toggleCerrado(final IncomeDAO row, final boolean isClose) throws SQLException {
        final IncomeDAO updatedIncome;

        if (isClose) {
            updatedIncome = row.toBuilder()
                    .id(row.id())
                    .cerrado(true)
                    .total(BigDecimal.ZERO)
                    .baseImponible(BigDecimal.ZERO)
                    .cuotaIva(BigDecimal.ZERO)
                    .numFactura(null)
                    .build();
        } else {
            updatedIncome = row.toBuilder()
                    .id(row.id())
                    .cerrado(false)
                    .build();
        }
        persistir(row, updatedIncome);
    }

    private void persistir(final IncomeDAO previous, final IncomeDAO updated) {
        try {
            for (final ObservableList<IncomeDAO> monthRows : rowsByMonth) {
                final int index = monthRows.indexOf(previous);
                if (index != -1) {
                    if (previous.id() == null) {
                        // Guardar y obtener el ID generado
                        Integer newId = incomeService.saveIncome(updated);
                        IncomeDAO withId = updated.toBuilder().id(newId).build();
                        monthRows.set(index, withId);
                        dbData.put(withId.fecha(), withId);
                    } else {
                        incomeService.updateIncome(updated);
                        monthRows.set(index, updated);
                        dbData.put(updated.fecha(), updated);
                    }
                    break;
                }
            }
            updateTotalAmounts();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private IncomeDAO emptyRow(final LocalDate date) {
        return IncomeDAO.builder()
                .id(null)
                .fecha(date)
                .numFactura(null)
                .total(BigDecimal.ZERO)
                .cuotaIva(BigDecimal.ZERO)
                .baseImponible(BigDecimal.ZERO)
                .cerrado(false)
                .build();
    }

    private void updateTotalAmounts() {
        BigDecimal totalAmount = BigDecimal.ZERO, baseAmount = BigDecimal.ZERO, ivaAmount = BigDecimal.ZERO;

        final int starterMonth = (quarter - 1) * 3 + 1;
        for (int i = 0; i < rowsByMonth.size(); i++) {
            final int month = starterMonth + i;
            final ObservableList<IncomeDAO> rows = rowsByMonth.get(i);

            BigDecimal monthTotal = BigDecimal.ZERO;
            for (var incomeRow : rows) {
                final BigDecimal total = incomeRow.total() != null ? incomeRow.total() : BigDecimal.ZERO;
                BigDecimal base = incomeRow.baseImponible() != null ? incomeRow.baseImponible() : BigDecimal.ZERO;
                BigDecimal iva = incomeRow.cuotaIva() != null ? incomeRow.cuotaIva() : BigDecimal.ZERO;
                totalAmount = totalAmount.add(total);
                baseAmount = baseAmount.add(base);
                ivaAmount = ivaAmount.add(iva);
                monthTotal = monthTotal.add(total);
            }

            final Label label = totalLabelsByMonth.get(month);
            if (label != null) {
                label.setText("TOTAL " + MONTHS_NAMES[month - 1].toUpperCase() + ": " + numberFormat.format(monthTotal));
            }
        }

        totalAmountQuarterLabel.setText(numberFormat.format(totalAmount));
        baseAmountQuarterLabel.setText(numberFormat.format(baseAmount));
        ivaAmountQuarterLabel.setText(numberFormat.format(ivaAmount));
    }

    @FXML
    private void exportarExcel() {
        new Alert(Alert.AlertType.INFORMATION, "Exportación a Excel próximamente.", ButtonType.OK).showAndWait();
    }
}