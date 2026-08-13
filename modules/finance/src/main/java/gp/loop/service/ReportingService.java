package gp.loop.service;

import gp.loop.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Reporting aggregates over {@code Orders} (revenue / profit by period). */
public class ReportingService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ISO_LOCAL_DATE;

    public double revenueAllTime() throws Exception {
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT IFNULL(SUM(TotalCost),0) AS s FROM finance_Orders")) {
            return rs.next() ? rs.getDouble("s") : 0;
        }
    }

    /**
     * Restricts a query to orders containing at least one item of the given dietary type, using
     * the Product component's {@code product_Category} table. Returns empty SQL when no dietary
     * filter is active, so the same queries serve both the filtered and unfiltered dashboard.
     */
    private static String dietaryClause(String dietary, String orderIdColumn) {
        if (dietary == null || dietary.isBlank()) {
            return "";
        }
        return " AND " + orderIdColumn + " IN ("
                + "SELECT oi2.OrderID FROM finance_OrderItem oi2 "
                + "JOIN product_Category c2 ON c2.productID = oi2.ProductID "
                + "WHERE c2.chosenDietary = ?)";
    }

    /** The dietary types the Product component currently offers, for the dashboard filter. */
    public java.util.List<String> availableDietaryTypes() throws Exception {
        java.util.List<String> types = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT chosenDietary FROM product_Category "
                + "WHERE chosenDietary IS NOT NULL AND TRIM(chosenDietary) <> '' "
                + "ORDER BY chosenDietary";
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                types.add(rs.getString(1));
            }
        } catch (Exception e) {
            /* Product tables are absent when this module runs standalone; filter stays empty. */
            return types;
        }
        return types;
    }

    public double revenueBetween(LocalDate startInclusive, LocalDate endExclusive, String dietary)
            throws Exception {
        String sql = "SELECT IFNULL(SUM(TotalCost),0) AS s FROM finance_Orders "
                + "WHERE date(Date) >= ? AND date(Date) < ?" + dietaryClause(dietary, "OrderID");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startInclusive.format(DAY));
            ps.setString(2, endExclusive.format(DAY));
            if (dietary != null && !dietary.isBlank()) {
                ps.setString(3, dietary);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    public int orderCountBetween(LocalDate startInclusive, LocalDate endExclusive, String dietary)
            throws Exception {
        String sql = "SELECT COUNT(*) AS c FROM finance_Orders "
                + "WHERE date(Date) >= ? AND date(Date) < ?" + dietaryClause(dietary, "OrderID");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startInclusive.format(DAY));
            ps.setString(2, endExclusive.format(DAY));
            if (dietary != null && !dietary.isBlank()) {
                ps.setString(3, dietary);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        }
    }

    public double profitBetween(LocalDate startInclusive, LocalDate endExclusive, String dietary)
            throws Exception {
        String sql = "SELECT IFNULL(SUM((IFNULL(oi.UnitPrice,0) - IFNULL(p.cost,0)) * IFNULL(oi.Quantity,0)), 0) AS s "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "LEFT JOIN finance_Orders o ON o.OrderID = oi.OrderID "
                + "WHERE date(o.Date) >= ? AND date(o.Date) < ?" + dietaryClause(dietary, "o.OrderID");
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startInclusive.format(DAY));
            ps.setString(2, endExclusive.format(DAY));
            if (dietary != null && !dietary.isBlank()) {
                ps.setString(3, dietary);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    /** Order IDs matching the dietary filter, used to narrow the dashboard table. */
    public java.util.Set<Long> orderIdsForDietary(String dietary) throws Exception {
        java.util.Set<Long> ids = new java.util.HashSet<>();
        if (dietary == null || dietary.isBlank()) {
            return ids;
        }
        String sql = "SELECT DISTINCT oi.OrderID FROM finance_OrderItem oi "
                + "JOIN product_Category c ON c.productID = oi.ProductID "
                + "WHERE c.chosenDietary = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dietary);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
            }
        }
        return ids;
    }

    /**
     * Revenue and profit per dietary type (Vegan, Halal, Vegetarian, Pescatarian), taken from the
     * Product component's {@code product_Category} table. Shows which dietary segments earn most.
     *
     * <p>A product may sit in more than one category, so a single order line can contribute to
     * more than one dietary row; the totals describe segments, not a partition of revenue.
     */
    public Map<String, DietaryTotals> revenueByDietary() throws Exception {
        Map<String, DietaryTotals> map = new LinkedHashMap<>();
        String sql = "SELECT c.chosenDietary AS dietary, "
                + "IFNULL(SUM(oi.Quantity * oi.UnitPrice), 0) AS revenue, "
                + "IFNULL(SUM(oi.Quantity * (oi.UnitPrice - IFNULL(p.cost, 0))), 0) AS profit "
                + "FROM finance_OrderItem oi "
                + "JOIN product_Category c ON c.productID = oi.ProductID "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "WHERE c.chosenDietary IS NOT NULL AND TRIM(c.chosenDietary) <> '' "
                + "GROUP BY c.chosenDietary ORDER BY revenue DESC";
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("dietary"),
                        new DietaryTotals(rs.getDouble("revenue"), rs.getDouble("profit")));
            }
        }
        return map;
    }

    /** Revenue and profit for one dietary segment. */
    public static final class DietaryTotals {
        private final double revenue;
        private final double profit;

        public DietaryTotals(double revenue, double profit) {
            this.revenue = revenue;
            this.profit = profit;
        }

        public double getRevenue() {
            return revenue;
        }

        public double getProfit() {
            return profit;
        }
    }

    public double revenueSince(LocalDate startInclusive) throws Exception {
        String day = startInclusive.format(DAY);
        String sql = "SELECT IFNULL(SUM(TotalCost),0) AS s FROM finance_Orders WHERE date(Date) >= ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, day);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    public double revenueBetween(LocalDate startInclusive, LocalDate endExclusive) throws Exception {
        String sql = "SELECT IFNULL(SUM(TotalCost),0) AS s FROM finance_Orders WHERE date(Date) >= ? AND date(Date) < ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startInclusive.format(DAY));
            ps.setString(2, endExclusive.format(DAY));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    public int orderCountBetween(LocalDate startInclusive, LocalDate endExclusive) throws Exception {
        String sql = "SELECT COUNT(*) AS c FROM finance_Orders WHERE date(Date) >= ? AND date(Date) < ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startInclusive.format(DAY));
            ps.setString(2, endExclusive.format(DAY));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        }
    }

    public double profitBetween(LocalDate startInclusive, LocalDate endExclusive) throws Exception {
        String sql = "SELECT IFNULL(SUM((IFNULL(oi.UnitPrice,0) - IFNULL(p.cost,0)) * IFNULL(oi.Quantity,0)), 0) AS s "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "LEFT JOIN finance_Orders o ON o.OrderID = oi.OrderID "
                + "WHERE date(o.Date) >= ? AND date(o.Date) < ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, startInclusive.format(DAY));
            ps.setString(2, endExclusive.format(DAY));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    public double profitAllTime() throws Exception {
        String sql = "SELECT IFNULL(SUM((oi.UnitPrice - IFNULL(p.cost,0)) * IFNULL(oi.Quantity,0)), 0) AS s "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID";
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("s") : 0;
        }
    }

    public double costSince(LocalDate startInclusive) throws Exception {
        String day = startInclusive.format(DAY);
        String sql = "SELECT IFNULL(SUM(IFNULL(p.cost,0) * IFNULL(oi.Quantity,0)),0) AS s "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "LEFT JOIN finance_Orders o ON o.OrderID = oi.OrderID "
                + "WHERE date(o.Date) >= ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, day);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    public double profitSince(LocalDate startInclusive) throws Exception {
        String day = startInclusive.format(DAY);
        String sql = "SELECT IFNULL(SUM((IFNULL(oi.UnitPrice,0) - IFNULL(p.cost,0)) * IFNULL(oi.Quantity,0)), 0) AS s "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "LEFT JOIN finance_Orders o ON o.OrderID = oi.OrderID "
                + "WHERE date(o.Date) >= ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, day);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("s") : 0;
            }
        }
    }

    public Map<String, Double> revenueByWeek() throws Exception {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT strftime('%Y-W%W', Date) AS w, SUM(TotalCost) AS s "
                + "FROM finance_Orders GROUP BY w ORDER BY w DESC";
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("w"), rs.getDouble("s"));
            }
        }
        return map;
    }

    public Map<String, Double> revenueByMonth() throws Exception {
        return monthlyTotals("SELECT strftime('%m', Date) AS m, SUM(TotalCost) AS s "
                + "FROM finance_Orders GROUP BY m ORDER BY m");
    }

    public Map<String, Double> costByMonth() throws Exception {
        return monthlyTotals("SELECT strftime('%m', o.Date) AS m, "
                + "IFNULL(SUM(IFNULL(p.cost,0) * IFNULL(oi.Quantity,0)),0) AS s "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "LEFT JOIN finance_Orders o ON o.OrderID = oi.OrderID "
                + "GROUP BY m ORDER BY m");
    }

    public Map<String, Double> orderCountByMonth() throws Exception {
        return monthlyTotals("SELECT strftime('%m', Date) AS m, COUNT(*) AS s "
                + "FROM finance_Orders GROUP BY m ORDER BY m");
    }

    private Map<String, Double> monthlyTotals(String sql) throws Exception {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            map.put(month.name().substring(0, 1) + month.name().substring(1, 3).toLowerCase(Locale.ROOT), 0.0);
        }
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String key = rs.getString("m");
                if (key == null) {
                    continue;
                }
                int monthIdx = Integer.parseInt(key);
                Month month = Month.of(monthIdx);
                String label = month.name().substring(0, 1) + month.name().substring(1, 3).toLowerCase(Locale.ROOT);
                map.put(label, rs.getDouble("s"));
            }
        }
        return map;
    }
}
