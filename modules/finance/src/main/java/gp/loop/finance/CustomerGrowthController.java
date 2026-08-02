package gp.loop.finance;

import java.io.IOException;
import java.util.Map;

import gp.loop.App;
import gp.loop.service.ReportingService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;

public class CustomerGrowthController {

    private final ReportingService reporting = new ReportingService();

    @FXML
    private BarChart<String, Number> chartCustomerGrowth;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            Map<String, Double> growthByMonth = reporting.orderCountByMonth();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Customers");
            for (String month : growthByMonth.keySet()) {
                series.getData().add(new XYChart.Data<>(month, growthByMonth.get(month)));
            }
            chartCustomerGrowth.getData().setAll(series);
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
