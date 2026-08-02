package gp.loop.finance;

import java.io.IOException;
import java.util.Map;

import gp.loop.App;
import gp.loop.service.ReportingService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;

public class RevenueVsCostsController {

    private final ReportingService reporting = new ReportingService();

    @FXML
    private LineChart<String, Number> chartRevenueVsCosts;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
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

            chartRevenueVsCosts.getData().setAll(revenueSeries, costsSeries);
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
