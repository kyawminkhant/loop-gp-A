package gp.loop.service;

import gp.loop.db.Database;
import gp.loop.model.OrderRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FinanceService {

    public List<OrderRow> listRecentOrders(int limit) throws Exception {
        List<OrderRow> rows = new ArrayList<>();
        String sql = "SELECT o.OrderID AS OrderID, o.Date AS OrderDate, o.TotalCost AS TotalAmount, "
                + "IFNULL(SUM(p.cost * oi.Quantity), 0) AS Cost "
                + "FROM finance_Orders o "
                + "LEFT JOIN finance_OrderItem oi ON oi.OrderID = o.OrderID "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID "
                + "GROUP BY o.OrderID, o.Date, o.TotalCost "
                + "ORDER BY datetime(o.Date) DESC LIMIT ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new OrderRow(
                            rs.getLong("OrderID"),
                            rs.getString("OrderDate"),
                            rs.getDouble("TotalAmount"),
                            rs.getDouble("Cost")));
                }
            }
        }
        return rows;
    }

    public void addDemoOrder() throws Exception {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long productId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO finance_Product(Name, Price, cost) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Weekly box (demo)");
                    ps.setDouble(2, 45.99);
                    ps.setDouble(3, 27.25);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        productId = keys.getLong(1);
                    }
                }

                long orderId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO finance_Orders(Date, TotalCost) VALUES (?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, LocalDateTime.now().toString());
                    ps.setDouble(2, 45.99);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO finance_OrderItem(OrderID, ProductID, Quantity, UnitPrice) VALUES (?,?,?,?)")) {
                    ps.setLong(1, orderId);
                    ps.setLong(2, productId);
                    ps.setInt(3, 1);
                    ps.setDouble(4, 45.99);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public double getTotalRevenue() throws Exception {
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT IFNULL(SUM(TotalCost),0) FROM finance_Orders")) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public double getTotalProfit() throws Exception {
        String sql = "SELECT IFNULL(SUM((oi.UnitPrice - IFNULL(p.cost,0)) * IFNULL(oi.Quantity,0)), 0) AS profit "
                + "FROM finance_OrderItem oi "
                + "LEFT JOIN finance_Product p ON p.ProductID = oi.ProductID";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}
