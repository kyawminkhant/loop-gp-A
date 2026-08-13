package gp.loop.finance;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import gp.loop.App;
import gp.loop.model.OrderRow;
import gp.loop.service.FinanceReporting;
import gp.loop.service.FinanceService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

public class FinanceController {

    private final FinanceService finance = new FinanceService();
    private final FinanceReporting financeReporting = new FinanceReporting();

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

        String headerPill = "-fx-background-color:rgba(27,94,32,0.35);-fx-background-radius:100;-fx-border-color:rgba(255,255,255,0.65);-fx-border-width:1;-fx-border-radius:100;-fx-padding:12 22;-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:#1a3a1a;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0.2,0,3);";
        javafx.application.Platform.runLater(() ->
            tableOrders.lookupAll(".column-header .label").forEach(node -> {
                if (node instanceof javafx.scene.control.Label) {
                    node.setStyle(headerPill);
                }
            })
        );
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            tableOrders.setItems(FXCollections.observableArrayList(finance.listRecentOrders(200)));
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void addDemoOrder() {
        try {
            finance.addDemoOrder();
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void exportCsvReport() {
        try {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(30);
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
    private void showExecutiveSummary() {
        try {
            String text = financeReporting.buildExecutiveSummaryText();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Finance & reporting");
            a.setHeaderText("Executive summary");
            TextArea ta = new TextArea(text);
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefRowCount(14);
            ta.setPrefColumnCount(42);
            a.getDialogPane().setContent(ta);
            a.getDialogPane().setPrefWidth(520);
            a.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void openDietaryBreakdown() throws IOException {
        App.setRoot("dietary-breakdown");
    }

    @FXML
    private void backToHome() throws IOException {
        App.setRoot("finance-dashboard");
    }
}
