package gp.loop.finance;

import java.io.IOException;
import java.util.Map;

import gp.loop.App;
import gp.loop.service.ReportingService;
import gp.loop.service.ReportingService.DietaryTotals;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Revenue and profit grouped by dietary type, joining the Product component's category data
 * to this component's order data.
 */
public class DietaryBreakdownController {

    private final ReportingService reporting = new ReportingService();

    @FXML
    private BarChart<String, Number> chartDietary;
    @FXML
    private Label labelSummary;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            Map<String, DietaryTotals> byDietary = reporting.revenueByDietary();

            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenue");
            XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
            profitSeries.setName("Profit");

            String best = null;
            double bestRevenue = 0;
            for (Map.Entry<String, DietaryTotals> entry : byDietary.entrySet()) {
                String dietary = entry.getKey();
                DietaryTotals totals = entry.getValue();
                revenueSeries.getData().add(new XYChart.Data<>(dietary, totals.getRevenue()));
                profitSeries.getData().add(new XYChart.Data<>(dietary, totals.getProfit()));
                if (totals.getRevenue() > bestRevenue) {
                    bestRevenue = totals.getRevenue();
                    best = dietary;
                }
            }

            chartDietary.getData().setAll(revenueSeries, profitSeries);

            if (byDietary.isEmpty()) {
                labelSummary.setText("No dietary data available yet.");
            } else {
                labelSummary.setText(String.format(
                        "%s meals earn the most: £%.2f revenue across %d dietary types.",
                        best, bestRevenue, byDietary.size()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void backToHome() throws IOException {
        App.setRoot("finance-dashboard");
    }
}
