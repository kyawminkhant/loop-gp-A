package gp.loop.finance;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import gp.loop.App;
import gp.loop.service.FinanceReporting;
import gp.loop.service.ReportingService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class FinancialOverviewController {

    private final ReportingService reporting = new ReportingService();
    private final FinanceReporting financeReporting = new FinanceReporting();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(Locale.UK);

    @FXML
    private Label labelStartupCosts;
    @FXML
    private Label labelMonthlyRevenue;
    @FXML
    private Label labelMonthlyCosts;
    @FXML
    private Label labelMonthlyProfit;

    @FXML
    private void initialize() {
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            double startupCosts = financeReporting.calculateProductCost();
            double monthlyRevenue = reporting.revenueSince(monthStart);
            double monthlyCosts = reporting.costSince(monthStart);
            double monthlyProfit = reporting.profitSince(monthStart);

            labelStartupCosts.setText(money.format(startupCosts));
            labelMonthlyRevenue.setText(money.format(monthlyRevenue));
            labelMonthlyCosts.setText(money.format(monthlyCosts));
            labelMonthlyProfit.setText(money.format(monthlyProfit));
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
