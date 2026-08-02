package gp.loop.finance;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import gp.loop.App;
import gp.loop.service.FinanceReporting;
import gp.loop.service.ReportingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;

public class CostBreakdownController {

    private final ReportingService reporting = new ReportingService();
    private final FinanceReporting financeReporting = new FinanceReporting();

    @FXML
    private PieChart pieCostBreakdown;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            double costs = Math.max(0, financeReporting.calculateProductCost());
            double tax = Math.max(0, financeReporting.calculateTax());
            double revenue = Math.max(0, reporting.revenueAllTime());
            double margin = Math.max(0, revenue - costs - tax);
            double total = costs + tax + margin;

            List<PieChart.Data> slices = List.of(
                    new PieChart.Data(labelWithPercent("COGS", costs, total), costs),
                    new PieChart.Data(labelWithPercent("Tax", tax, total), tax),
                    new PieChart.Data(labelWithPercent("Margin", margin, total), margin));

            pieCostBreakdown.setData(FXCollections.observableArrayList(slices));
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private String labelWithPercent(String name, double value, double total) {
        double percent = total > 0 ? (value / total) * 100.0 : 0.0;
        return String.format(Locale.US, "%s (%.1f%%)", name, percent);
    }

    @FXML
    private void backToHome() throws IOException {
        App.setRoot("finance-dashboard");
    }
}
