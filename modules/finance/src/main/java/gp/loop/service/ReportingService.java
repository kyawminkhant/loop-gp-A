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
