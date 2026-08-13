package gp.loop.finance;

import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import gp.loop.App;
import gp.loop.model.OrderRow;
import gp.loop.service.FinanceReporting;
import gp.loop.service.FinanceService;
import gp.loop.service.ReportingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/** Finance hub: mock layout, glass styling, click-through navigation to detail roots. */
public class FinanceDashboardController {

    private final ReportingService reporting = new ReportingService();
    private final FinanceService finance = new FinanceService();
    private final FinanceReporting financeReporting = new FinanceReporting();

    private static final NumberFormat GBP = NumberFormat.getCurrencyInstance(Locale.UK);

    private int dateRangeDays = 30;

    /** Selected dietary type (Vegan, Halal, ...), or {@code null} for all customers. */
    private String dietaryFilter;

    @FXML
    private MenuButton menuDateRange;
    @FXML
    private MenuButton menuCustomer;
    @FXML
    private Label kpiRevenueValue;
    @FXML
    private Label kpiRevenueDelta;
    @FXML
    private Label kpiOrdersValue;
    @FXML
    private Label kpiOrdersDelta;
    @FXML
    private Label kpiProfitValue;
    @FXML
    private Label kpiProfitDelta;
    @FXML
    private Label kpiAovValue;
    @FXML
    private Label kpiAovDelta;
    @FXML
    private StackPane paneLineChart;
    @FXML
    private StackPane paneBarChart;
    @FXML
    private StackPane panePieChart;
    @FXML
    private StackPane paneTable;
    @FXML
    private LineChart<String, Number> chartLine;
    @FXML
    private BarChart<String, Number> chartBar;
    @FXML
    private PieChart chartPie;
    @FXML
    private TableView<OrderRow> tableOrders;
    @FXML
    private TableColumn<OrderRow, String> colDate;
    @FXML
    private TableColumn<OrderRow, String> colTotal;
    @FXML
    private TableColumn<OrderRow, String> colCost;
    @FXML
    private TableColumn<OrderRow, String> colProfit;

    @FXML
    private void initialize() {
        tableOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDateDisplay"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalDisplay"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costDisplay"));
        colProfit.setCellValueFactory(new PropertyValueFactory<>("profitDisplay"));

        String greenPill = "-fx-background-color:rgba(27,94,32,0.30);-fx-background-radius:100;-fx-background-insets:2 4;-fx-border-color:rgba(255,255,255,0.55);-fx-border-width:1;-fx-border-radius:100;-fx-effect:innershadow(gaussian,rgba(255,255,255,0.45),6,0.15,0,-1);";
        String orangePill = "-fx-background-color:rgba(255,136,0,0.30);-fx-background-radius:100;-fx-background-insets:2 4;-fx-border-color:rgba(255,255,255,0.55);-fx-border-width:1;-fx-border-radius:100;-fx-effect:innershadow(gaussian,rgba(255,255,255,0.45),6,0.15,0,-1);";
        colDate.setCellFactory(col -> new StyledCell<>(greenPill));
        colTotal.setCellFactory(col -> new StyledCell<>(orangePill));
        colCost.setCellFactory(col -> new StyledCell<>(greenPill));
        colProfit.setCellFactory(col -> new StyledCell<>(orangePill));
        Label emptyHint = new Label("No orders in this date range.\nClick anywhere here to open the full revenue table.");
        emptyHint.getStyleClass().add("dashboard-table-placeholder");
        emptyHint.setWrapText(true);
        tableOrders.setPlaceholder(emptyHint);
        clipToRoundedBorder(paneLineChart, 100);
        clipToRoundedBorder(paneBarChart, 100);
        wireDashboardNavigation();
        wireTooltips();
        populateDietaryMenu();
        refreshAll();
    }

    /**
     * Fills the customer filter with the dietary types the Product component offers, so the
     * dashboard can be narrowed to customers who ordered vegan, halal, vegetarian food and so on.
     * The menu is hidden when those tables are unavailable (this module running standalone).
     */
    private void populateDietaryMenu() {
        if (menuCustomer == null) {
            return;
        }
        try {
            List<String> types = reporting.availableDietaryTypes();
            menuCustomer.getItems().clear();

            if (types.isEmpty()) {
                menuCustomer.setVisible(false);
                menuCustomer.setManaged(false);
                return;
            }

            javafx.scene.control.MenuItem all = new javafx.scene.control.MenuItem("All customers");
            all.setOnAction(e -> applyDietaryFilter(null));
            menuCustomer.getItems().add(all);
            menuCustomer.getItems().add(new javafx.scene.control.SeparatorMenuItem());

            for (String type : types) {
                javafx.scene.control.MenuItem item =
                        new javafx.scene.control.MenuItem("Ordered " + type);
                item.setOnAction(e -> applyDietaryFilter(type));
                menuCustomer.getItems().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            menuCustomer.setVisible(false);
            menuCustomer.setManaged(false);
        }
    }

    private void applyDietaryFilter(String dietary) {
        dietaryFilter = dietary;
        menuCustomer.setText(dietary == null ? "Customer: All" : "Customer: " + dietary);
        refreshAll();
    }

    private void wireTooltips() {
        Tooltip.install(paneLineChart, new Tooltip("Open revenue vs costs"));
        Tooltip.install(paneBarChart, new Tooltip("Open customer growth"));
        Tooltip.install(panePieChart, new Tooltip("Open cost breakdown"));
        Tooltip.install(paneTable, new Tooltip("Open revenue by week (full table)"));
    }

    /** Capture-phase clicks so chart/table internals still open the right screen. */
    private void wireDashboardNavigation() {
        attachNav(paneLineChart, "revenue-vs-costs");
        attachNav(paneBarChart, "customer-growth");
        attachNav(panePieChart, "cost-breakdown");
        attachNav(paneTable, "finance");
    }

    private void attachNav(StackPane pane, String fxml) {
        pane.setCursor(Cursor.HAND);
        pane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> navigateTo(fxml));
    }

    private void navigateTo(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void refreshAll() {
        try {
            refreshKpis();
            refreshLineChart();
            refreshBarChart();
            refreshPie();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private LocalDate windowStartInclusive() {
        LocalDate today = LocalDate.now();
        return today.minusDays(Math.max(1, dateRangeDays) - 1L);
    }

    private LocalDate windowEndExclusive() {
        return LocalDate.now().plusDays(1);
    }

    private void refreshKpis() throws Exception {
        LocalDate start = windowStartInclusive();
        LocalDate endEx = windowEndExclusive();
        LocalDate prevStart = start.minusDays(dateRangeDays);
        LocalDate prevEndEx = start;

        double rev = reporting.revenueBetween(start, endEx, dietaryFilter);
        double revPrev = reporting.revenueBetween(prevStart, prevEndEx, dietaryFilter);
        int ord = reporting.orderCountBetween(start, endEx, dietaryFilter);
        int ordPrev = reporting.orderCountBetween(prevStart, prevEndEx, dietaryFilter);
        double prof = reporting.profitBetween(start, endEx, dietaryFilter);
        double profPrev = reporting.profitBetween(prevStart, prevEndEx, dietaryFilter);

        double aov = ord > 0 ? rev / ord : 0;
        double aovPrev = ordPrev > 0 ? revPrev / ordPrev : 0;

        kpiRevenueValue.setText(GBP.format(rev));
        kpiOrdersValue.setText(Integer.toString(ord));
        kpiProfitValue.setText(GBP.format(prof));
        kpiAovValue.setText(GBP.format(aov));

        setDelta(kpiRevenueDelta, rev, revPrev);
        setDelta(kpiOrdersDelta, ord, ordPrev);
        setDelta(kpiProfitDelta, prof, profPrev);
        setDelta(kpiAovDelta, aov, aovPrev);
    }

    private void setDelta(Label label, double current, double previous) {
        label.getStyleClass().removeAll("dashboard-kpi-delta-pos", "dashboard-kpi-delta-neg");
        if (!label.getStyleClass().contains("dashboard-kpi-delta-bubble")) {
            label.getStyleClass().add("dashboard-kpi-delta-bubble");
        }
        if (previous > 1e-6) {
            double pct = (current - previous) / previous * 100.0;
            label.setText(String.format(Locale.US, "%.0f%%", Math.abs(pct)));
        } else if (current > 1e-6) {
            label.setText("100%");
        } else {
            label.setText("0%");
        }
        label.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        if (current + 1e-6 >= previous) {
            label.getStyleClass().add("dashboard-kpi-delta-pos");
            label.setStyle("-fx-background-color:linear-gradient(to bottom, #d8ffc8, #c3f5b4, #a8ea99);-fx-background-radius:100;-fx-padding:9 24 9 24;-fx-border-color:rgba(255,255,255,0.65);-fx-border-width:1.2;-fx-border-radius:100;-fx-font-size:16px;-fx-font-weight:800;-fx-text-fill:#000000;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),14,0.3,0,4);");
        } else {
            label.getStyleClass().add("dashboard-kpi-delta-neg");
            label.setStyle("-fx-background-color:rgba(220,40,40,0.75);-fx-background-radius:100;-fx-padding:9 24 9 24;-fx-border-color:rgba(255,255,255,0.65);-fx-border-width:1.2;-fx-border-radius:100;-fx-font-size:16px;-fx-font-weight:800;-fx-text-fill:#ffffff;-fx-effect:dropshadow(gaussian,rgba(160,40,40,0.18),14,0.3,0,4);");
        }
    }

    private void refreshLineChart() throws Exception {
        Map<String, Double> revenueByMonth = reporting.revenueByMonth();
        Map<String, Double> costByMonth = reporting.costByMonth();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        XYChart.Series<String, Number> costsSeries = new XYChart.Series<>();
        costsSeries.setName("Costs");
        for (String month : revenueByMonth.keySet()) {
            revenueSeries.getData().add(new XYChart.Data<>(month, revenueByMonth.getOrDefault(month, 0.0)));
            costsSeries.getData().add(new XYChart.Data<>(month, costByMonth.getOrDefault(month, 0.0)));
        }
        chartLine.getData().setAll(revenueSeries, costsSeries);
        javafx.application.Platform.runLater(() -> {
            if (chartLine.getData().size() >= 2) {
                javafx.scene.Node line0 = chartLine.getData().get(0).getNode();
                if (line0 != null) line0.setStyle("-fx-stroke: #7C4DFF; -fx-stroke-width: 3px;");
                javafx.scene.Node line1 = chartLine.getData().get(1).getNode();
                if (line1 != null) line1.setStyle("-fx-stroke: #F08A1A; -fx-stroke-width: 3px;");
            }
        });
    }

    private void refreshBarChart() throws Exception {
        Map<String, Double> byMonth = reporting.orderCountByMonth();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Customer growth");
        for (String month : byMonth.keySet()) {
            series.getData().add(new XYChart.Data<>(month, byMonth.get(month)));
        }
        chartBar.getData().setAll(series);
    }

    private void refreshPie() throws Exception {
        double costs = Math.max(0, financeReporting.calculateProductCost());
        double tax = Math.max(0, financeReporting.calculateTax());
        double revenue = Math.max(0, reporting.revenueAllTime());
        double margin = Math.max(0, revenue - costs - tax);
        double total = costs + tax + margin;
        List<PieChart.Data> slices = List.of(
                new PieChart.Data(labelPct("COGS", costs, total), costs),
                new PieChart.Data(labelPct("Tax", tax, total), tax),
                new PieChart.Data(labelPct("Margin", margin, total), margin));
        chartPie.setData(FXCollections.observableArrayList(slices));
    }

    private static String labelPct(String name, double value, double total) {
        double percent = total > 0 ? (value / total) * 100.0 : 0.0;
        return String.format(Locale.US, "%s (%.1f%%)", name, percent);
    }

    private void refreshTable() throws Exception {
        LocalDate start = windowStartInclusive();
        LocalDate today = LocalDate.now();
        java.util.Set<Long> allowed = reporting.orderIdsForDietary(dietaryFilter);
        List<OrderRow> rows = finance.listRecentOrders(500).stream()
                .filter(o -> o.getOrderDateLocal().map(d -> !d.isBefore(start) && !d.isAfter(today)).orElse(false))
                .filter(o -> dietaryFilter == null || allowed.contains(o.getOrderId()))
                .limit(80)
                .collect(Collectors.toList());
        tableOrders.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML
    private void dateRange7() {
        applyDateRange(7);
    }

    @FXML
    private void dateRange30() {
        applyDateRange(30);
    }

    @FXML
    private void dateRange90() {
        applyDateRange(90);
    }

    private void applyDateRange(int days) {
        dateRangeDays = days;
        menuDateRange.setText("Date Range: Last " + days + (days == 1 ? " Day" : " Days"));
        refreshAll();
    }

    @FXML
    private void exportCsv() {
        try {
            LocalDate end = LocalDate.now();
            LocalDate start = windowStartInclusive();
            Path out = financeReporting.defaultReportPath(start, end);
            financeReporting.filterReport(
                    LocalDateTime.of(start, LocalTime.MIN),
                    LocalDateTime.of(end, LocalTime.of(23, 59, 59)),
                    null,
                    null,
                    out);
            new Alert(Alert.AlertType.INFORMATION, "CSV written to:\n" + out.toAbsolutePath()).showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void openFinancialOverview() throws IOException {
        App.setRoot("financial-overview");
    }

    @FXML
    private void openBreakEven() throws IOException {
        App.setRoot("break-even-analysis");
    }

    @FXML
    private void openLocationPerformance() throws IOException {
        App.setRoot("location-performance");
    }

    private void clipToRoundedBorder(StackPane pane, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        pane.setClip(clip);
    }
}
