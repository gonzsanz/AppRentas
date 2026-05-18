package com.apprenta.renta.controller;

import com.apprenta.renta.controller.calculator.QuarterTotalsCalculator;
import com.apprenta.renta.controller.handler.IncomePersistenceHandler;
import com.apprenta.renta.controller.table.IncomeTableBuilder;
import com.apprenta.renta.model.MonthTotal;
import com.apprenta.renta.model.QuarterTotal;
import com.apprenta.renta.model.dao.IncomeDAO;
import com.apprenta.renta.repository.IncomeRepository;
import com.apprenta.renta.service.IncomeService;
import com.apprenta.renta.service.IncomeServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Controlador de la pantalla de ingresos.
 * <p>
 * Responsabilidad única: coordinar la carga del trimestre, delegar la construcción
 * de la UI en {@link IncomeTableBuilder} y la persistencia en {@link IncomePersistenceHandler},
 * y actualizar las etiquetas de totales con ayuda de {@link QuarterTotalsCalculator}.
 * <p>
 */
public class IncomeController implements Initializable {

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
    private final Map<Integer, Label> totalLabels;

    private IncomePersistenceHandler persistenceHandler;

    private static final String[] MONTH_NAMES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public IncomeController() {
        this(new IncomeServiceImpl(new IncomeRepository()));
    }

    public IncomeController(final IncomeService incomeService) {
        this.incomeService = incomeService;
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        this.numberFormat.setMinimumFractionDigits(2);
        rowsByMonth = new ArrayList<>();
        dbData = new HashMap<>();
        totalLabels = new HashMap<>();
    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        year = LocalDate.now().getYear();
        quarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;

        persistenceHandler = new IncomePersistenceHandler(
                incomeService, rowsByMonth, dbData, this::updateTotalAmounts
        );

        initializeCombo();
        cbQuarterYear.setValue(String.valueOf(year));
        cbQuarter.setValue(quarter + "T");
        loadQuarter();
    }

    private void initializeCombo() {
        final List<String> years = new ArrayList<>();
        final List<String> quarters = new ArrayList<>();

        for (int y = year - 2; y <= year; y++) years.add(String.valueOf(y));
        for (int q = 1; q <= 4; q++) quarters.add(q + "T");

        cbQuarterYear.setItems(FXCollections.observableArrayList(years));
        cbQuarter.setItems(FXCollections.observableArrayList(quarters));
    }

    @FXML
    private void selectYearCombo() {
        final String value = cbQuarterYear.getValue();
        if (value == null) return;
        year = Integer.parseInt(value);
        loadQuarter();
    }

    @FXML
    private void selectQuarterCombo() {
        final String value = cbQuarter.getValue();
        if (value == null) return;
        quarter = Integer.parseInt(value.substring(0, 1));
        loadQuarter();
    }

    private void loadQuarter() {
        try {
            clearState();
            loadDbData();

            final int startMonth = (quarter - 1) * 3 + 1;
            for (int i = 0; i < 3; i++) {
                final int month = startMonth + i;
                final ObservableList<IncomeDAO> rows = buildMonthRows(month);
                rowsByMonth.add(rows);
                monthContainer.getChildren().add(
                        IncomeTableBuilder.buildMonthBlock(
                                month, year, rows,
                                persistenceHandler, numberFormat,
                                label -> totalLabels.put(month, label)
                        )
                );
            }
            updateTotalAmounts();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearState() {
        dbData.clear();
        rowsByMonth.clear();
        monthContainer.getChildren().clear();
        totalLabels.clear();
    }

    private void loadDbData() throws Exception {
        incomeService.listByQuarter(year, quarter)
                .forEach(dao -> dbData.put(dao.fecha(), dao));
    }

    private ObservableList<IncomeDAO> buildMonthRows(final int month) {
        final ObservableList<IncomeDAO> rows = FXCollections.observableArrayList();
        final LocalDate firstDay = LocalDate.of(year, month, 1);
        for (int d = 1; d <= firstDay.lengthOfMonth(); d++) {
            final LocalDate date = LocalDate.of(year, month, d);
            rows.add(dbData.getOrDefault(date, IncomeDAO.emptyFor(date)));
        }
        return rows;
    }

    private void updateTotalAmounts() {
        final int startMonth = (quarter - 1) * 3 + 1;

        for (final MonthTotal mt : QuarterTotalsCalculator.byMonth(rowsByMonth, startMonth)) {
            final Label label = totalLabels.get(mt.month());
            if (label != null) {
                label.setText("TOTAL " + MONTH_NAMES[mt.month() - 1].toUpperCase()
                        + ": " + numberFormat.format(mt.totalAmount()));
            }
        }

        final QuarterTotal totals = QuarterTotalsCalculator.calculate(rowsByMonth);
        totalAmountQuarterLabel.setText(numberFormat.format(totals.totalAmount()));
        baseAmountQuarterLabel.setText(numberFormat.format(totals.baseAmount()));
        ivaAmountQuarterLabel.setText(numberFormat.format(totals.ivaAmount()));
    }

    @FXML
    private void exportarExcel() {
        new Alert(Alert.AlertType.INFORMATION, "Exportación a Excel próximamente.", ButtonType.OK)
                .showAndWait();
    }
}