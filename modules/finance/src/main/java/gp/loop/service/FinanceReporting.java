package gp.loop.service;

import gp.loop.db.Database;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Finance &amp; reporting slice aligned with the group UML. Uses current SQLite schema:
 * {@code Orders}, {@code OrderItem}, {@code Product}. Customer/category UUID filters apply when
 * teammates add those columns; until then they are ignored.
 */
public class FinanceReporting {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final double DEFAULT_TAX_RATE = 0.0825;

    /** UML: {@code totalSales} — here: count of orders (adjust to SUM if your team uses sales $). */
    public int getTotalSales() throws Exception {
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM finance_Orders")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** UML: {@code incomeSummaries} — total order revenue ({@code Orders.TotalCost}). */
    public double getIncomeSummaries() throws Exception {
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT IFNULL(SUM(TotalCost),0) FROM finance_Orders")) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /**
     * UML: {@code refunds}. No refunds table yet — returns {@code 0} until the group adds one.
     */
    public double getRefunds() {
        return 0;
    }

    /**
     * UML: {@code outstandingPay}. No payables schema yet — returns {@code 0} until logistics/billing adds it.
     */
    public double getOutstandingPay() {
        return 0;
    }

    /** UML: {@code calculateProductCost()} — COGS from line items ({@code cost * quantity}). */
    public double calculateProductCost() throws Exception {
        String sql = "SELECT IFNULL(SUM(IFNULL(p.cost,0) * IFNULL(oi.Quantity,0)),0) AS c "
                + "FROM finance_OrderItem oi JOIN finance_Product p ON p.ProductID = oi.ProductID";
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("c") : 0;
        }
    }

    /** UML: {@code calculateSalePrice()} — gross sales from line items ({@code UnitPrice * quantity}). */
    public double calculateSalePrice() throws Exception {
        String sql = "SELECT IFNULL(SUM(IFNULL(oi.UnitPrice,0) * IFNULL(oi.Quantity,0)),0) AS s FROM finance_OrderItem oi";
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("s") : 0;
        }
    }

    /** UML: {@code calculateProfitMargin(profit, cost)} — margin as percentage (0–100+). */
    public double calculateProfitMargin(double profit, double cost) {
        if (cost <= 0) {
            return profit > 0 ? 100.0 : 0.0;
        }
        return 100.0 * (profit / cost);
    }

    /** UML: {@code calculateTax()} — simple rate on {@link #getIncomeSummaries()}. */
    public double calculateTax() throws Exception {
        return getIncomeSummaries() * DEFAULT_TAX_RATE;
    }

    /**
     * UML: {@code filterReport(...): .csv/.pdf} — exports {@code .csv} for the date range.
     * {@code customerID} / {@code categoryID} reserved for future schema (ignored if unsupported).
     */
    public Path filterReport(
            LocalDateTime startDate,
            LocalDateTime endDate,
            UUID customerID,
            UUID categoryID,
            Path csvDestination) throws Exception {
        String start = startDate.format(ISO_DT);
        String end = endDate.format(ISO_DT);
        String sql = "SELECT o.OrderID, o.Date, o.TotalCost, p.Name, oi.Quantity, oi.UnitPrice, IFNULL(p.cost,0) AS cost "
                + "FROM finance_Orders o "
                + "LEFT JOIN finance_OrderItem oi ON oi.OrderID = o.OrderID "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "WHERE datetime(o.Date) >= datetime(?) AND datetime(o.Date) <= datetime(?) "
                + "ORDER BY o.OrderID, oi.OrderItemID";
        Files.createDirectories(csvDestination.getParent() != null ? csvDestination.getParent() : Path.of("."));
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                BufferedWriter w = Files.newBufferedWriter(csvDestination, StandardCharsets.UTF_8)) {
            ps.setString(1, start);
            ps.setString(2, end);
            w.write("# Loop finance export (CSV). customerID=");
            w.write(customerID != null ? customerID.toString() : "null");
            w.write(" categoryID=");
            w.write(categoryID != null ? categoryID.toString() : "null");
            w.newLine();
            w.write("OrderID,OrderDate,OrderTotal,ProductName,Quantity,UnitPrice,ProductCost");
            w.newLine();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    w.write(String.format(Locale.US, "%d,%s,%.2f,%s,%d,%.2f,%.2f",
                            rs.getLong("OrderID"),
                            escapeCsv(rs.getString("Date")),
                            rs.getDouble("TotalCost"),
                            escapeCsv(rs.getString("Name")),
                            rs.getInt("Quantity"),
                            rs.getDouble("UnitPrice"),
                            rs.getDouble("cost")));
                    w.newLine();
                }
            }
        }
        return csvDestination;
    }

    private static String escapeCsv(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\"") || t.contains("\n")) {
            return "\"" + t + "\"";
        }
        return t;
    }

    /** Names of top products by units sold (UML “popular products”). */
    public List<String> getPopularProductNames(int limit) throws Exception {
        List<String> names = new ArrayList<>();
        String sql = "SELECT p.Name AS n, SUM(oi.Quantity) AS q "
                + "FROM finance_OrderItem oi JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "GROUP BY p.ProductID ORDER BY q DESC LIMIT ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("n"));
                }
            }
        }
        return names;
    }

    /**
     * UML: {@code print(cumulativeIncome, noOfOrder, averageOrderValue, popularProducts)}.
     * Returns {@code averageOrderValue}; also builds a human-readable summary string for the UI.
     */
    public double printSummaryLine(
            double cumulativeIncome,
            int noOfOrder,
            double averageOrderValue,
            String[] popularProducts) {
        return averageOrderValue;
    }

    /** Convenience: KPIs for the summary “print” line. */
    public String buildExecutiveSummaryText() throws Exception {
        double income = getIncomeSummaries();
        int orders = getTotalSales();
        double aov = orders > 0 ? income / orders : 0;
        double cogs = calculateProductCost();
        double profit = income - cogs;
        double margin = calculateProfitMargin(profit, cogs);
        double tax = calculateTax();
        List<String> popular = getPopularProductNames(5);
        String[] pop = popular.toArray(new String[0]);
        printSummaryLine(income, orders, aov, pop);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Cumulative income: $%.2f%n", income));
        sb.append(String.format(Locale.US, "finance_Orders: %d%n", orders));
        sb.append(String.format(Locale.US, "Average order value: $%.2f%n", aov));
        sb.append(String.format(Locale.US, "COGS: $%.2f | Gross profit: $%.2f | Margin vs cost: %.1f%%%n", cogs, profit, margin));
        sb.append(String.format(Locale.US, "Estimated tax (%.2f%%): $%.2f%n", DEFAULT_TAX_RATE * 100, tax));
        sb.append(String.format(Locale.US, "Refunds (placeholder): $%.2f | Outstanding pay (placeholder): $%.2f%n",
                getRefunds(), getOutstandingPay()));
        sb.append("Popular products: ");
        sb.append(popular.isEmpty() ? "—" : String.join(", ", popular));
        return sb.toString();
    }

    /** Default CSV path on Desktop for quick demos. */
    public Path defaultReportPath(LocalDate start, LocalDate end) {
        String home = System.getProperty("user.home");
        String name = String.format(Locale.US, "loop-finance-%s_to_%s.csv", start, end);
        return Path.of(home, "Desktop", name);
    }
}
