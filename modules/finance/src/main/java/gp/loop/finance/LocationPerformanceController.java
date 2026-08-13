package gp.loop.finance;

import gp.loop.App;
import gp.loop.model.LocationFinanceRow;
import gp.loop.service.LocationFinanceService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Finance view that joins orders and costs to Inventory warehouse locations. */
public final class LocationPerformanceController {

    private static final NumberFormat GBP = NumberFormat.getCurrencyInstance(Locale.UK);
    private final LocationFinanceService service = new LocationFinanceService();

    @FXML private Label locationCountValue;
    @FXML private Label revenueValue;
    @FXML private Label profitValue;
    @FXML private Label stockValue;
    @FXML private Label unavailableValue;
    @FXML private Label inboundValue;
    @FXML private Label dataNote;
    @FXML private BarChart<String, Number> locationChart;
    @FXML private TableView<LocationFinanceRow> locationTable;
    @FXML private TableColumn<LocationFinanceRow, String> locationColumn;
    @FXML private TableColumn<LocationFinanceRow, String> warehouseColumn;
    @FXML private TableColumn<LocationFinanceRow, String> ordersColumn;
    @FXML private TableColumn<LocationFinanceRow, String> revenueColumn;
    @FXML private TableColumn<LocationFinanceRow, String> costColumn;
    @FXML private TableColumn<LocationFinanceRow, String> profitColumn;
    @FXML private TableColumn<LocationFinanceRow, String> stockColumn;
    @FXML private TableColumn<LocationFinanceRow, String> unavailableColumn;
    @FXML private TableColumn<LocationFinanceRow, String> inboundColumn;

    @FXML
    private void initialize() {
        locationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationDisplay"));
        warehouseColumn.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        ordersColumn.setCellValueFactory(new PropertyValueFactory<>("ordersDisplay"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenueDisplay"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("costDisplay"));
        profitColumn.setCellValueFactory(new PropertyValueFactory<>("profitDisplay"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockDisplay"));
        unavailableColumn.setCellValueFactory(new PropertyValueFactory<>("unavailableDisplay"));
        inboundColumn.setCellValueFactory(new PropertyValueFactory<>("inboundDisplay"));
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            List<LocationFinanceRow> rows = service.listLocationPerformance();
            locationTable.setItems(FXCollections.observableArrayList(rows));
            refreshSummary(rows);
            refreshChart(rows);
            dataNote.setText(rows.isEmpty()
                    ? "No Inventory warehouse data is available. Open Inventory once to initialise it."
                    : "Orders use the warehouse serving the customer's delivery address. "
                      + "Guest and unmatched addresses use Central London.");
        } catch (Exception exception) {
            exception.printStackTrace();
            new Alert(Alert.AlertType.ERROR, exception.getMessage()).showAndWait();
        }
    }

    private void refreshSummary(List<LocationFinanceRow> rows) {
        double revenue = rows.stream().mapToDouble(LocationFinanceRow::getRevenue).sum();
        double profit = rows.stream().mapToDouble(LocationFinanceRow::getProfit).sum();
        int stock = rows.stream().mapToInt(LocationFinanceRow::getStockUnits).sum();
        int unavailable = rows.stream()
                .mapToInt(LocationFinanceRow::getUnavailableIngredients).sum();
        int activeDeliveries = rows.stream()
                .mapToInt(LocationFinanceRow::getActiveDeliveries).sum();
        int inboundUnits = rows.stream().mapToInt(LocationFinanceRow::getInboundUnits).sum();

        locationCountValue.setText(Integer.toString(rows.size()));
        revenueValue.setText(GBP.format(revenue));
        profitValue.setText(GBP.format(profit));
        stockValue.setText(String.format(Locale.UK, "%,d", stock));
        unavailableValue.setText(Integer.toString(unavailable));
        inboundValue.setText(String.format(
                Locale.UK, "%d / %,d units", activeDeliveries, inboundUnits));
    }

    private void refreshChart(List<LocationFinanceRow> rows) {
        XYChart.Series<String, Number> revenue = new XYChart.Series<>();
        revenue.setName("Revenue");
        XYChart.Series<String, Number> profit = new XYChart.Series<>();
        profit.setName("Profit");
        for (LocationFinanceRow row : rows) {
            revenue.getData().add(new XYChart.Data<>(row.getServiceArea(), row.getRevenue()));
            profit.getData().add(new XYChart.Data<>(row.getServiceArea(), row.getProfit()));
        }
        locationChart.getData().setAll(revenue, profit);
    }

    @FXML
    private void exportCsv() {
        try {
            Path output = Path.of(
                    System.getProperty("user.home"),
                    "Desktop",
                    "loop-finance-uk-locations-" + LocalDate.now() + ".csv");
            service.exportCsv(output);
            new Alert(Alert.AlertType.INFORMATION,
                    "UK location report written to:\n" + output.toAbsolutePath()).showAndWait();
        } catch (Exception exception) {
            exception.printStackTrace();
            new Alert(Alert.AlertType.ERROR, exception.getMessage()).showAndWait();
        }
    }

    @FXML
    private void backToDashboard() throws IOException {
        App.setRoot("finance-dashboard");
    }
}
