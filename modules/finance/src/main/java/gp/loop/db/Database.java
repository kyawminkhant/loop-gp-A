package gp.loop.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class Database {

    /**
     * Default: {@code ~/Desktop/DATA.db} (same idea as DB Browser: .../Desktop/DATA.db).
     * Override: {@code -Dloop.db.path=/absolute/path/to/file.db}
     */
    private static final Path DB_FILE = resolvePath();

    private Database() {
    }

    private static Path resolvePath() {
        String override = System.getProperty("loop.db.path");
        if (override != null && !override.isBlank()) {
            return Path.of(override.trim()).toAbsolutePath();
        }
        return Path.of("database", "loop.db").toAbsolutePath();
    }

    public static Path filePath() {
        return DB_FILE;
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_FILE.toAbsolutePath());
    }

    public static void initialize() throws Exception {
        Path parent = DB_FILE.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS finance_Orders ("
                            + "OrderID INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "Date TEXT, "
                            + "TotalCost REAL)");
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS finance_Product ("
                            + "ProductID INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "Name TEXT, "
                            + "Price REAL, "
                            + "cost REAL)");
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS finance_OrderItem ("
                            + "OrderItemID INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "OrderID INTEGER, "
                            + "ProductID INTEGER, "
                            + "Quantity INTEGER, "
                            + "UnitPrice REAL)");
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS finance_Users ("
                            + "UserID INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "Email TEXT UNIQUE, "
                            + "PasswordHash TEXT)");
        }
        seedIfEmpty();
        seedAdminIfNeeded();
        System.out.println("Database initialized at " + filePath());
    }

    private static void seedIfEmpty() throws Exception {
        try (Connection c = getConnection()) {
            if (tableCount(c, "finance_Orders") == 0 && tableCount(c, "finance_Product") == 0 && tableCount(c, "finance_OrderItem") == 0) {
                seedDemoOrder(c, "Weekly box", 45.99, 27.25, "2026-03-03T10:00:00");
                seedDemoOrder(c, "Weekly box", 45.99, 27.25, "2026-03-10T10:00:00");
                seedDemoOrder(c, "Add-on snacks", 12.99, 6.00, "2026-03-11T18:22:00");
            }
        }
    }

    private static int tableCount(Connection c, String table) throws Exception {
        try (Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) AS n FROM " + table)) {
            return rs.next() ? rs.getInt("n") : 0;
        }
    }

    private static void seedDemoOrder(Connection c, String productName, double unitPrice, double unitCost, String when)
            throws Exception {
        c.setAutoCommit(false);
        try {
            long productId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO finance_Product(Name, Price, cost) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, productName);
                ps.setDouble(2, unitPrice);
                ps.setDouble(3, unitCost);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    productId = keys.getLong(1);
                }
            }

            long orderId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO finance_Orders(Date, TotalCost) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, when);
                ps.setDouble(2, unitPrice);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    orderId = keys.getLong(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO finance_OrderItem(OrderID, ProductID, Quantity, UnitPrice) VALUES (?,?,?,?)")) {
                ps.setLong(1, orderId);
                ps.setLong(2, productId);
                ps.setInt(3, 1);
                ps.setDouble(4, unitPrice);
                ps.executeUpdate();
            }

            c.commit();
        } catch (Exception e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    }

    /** Seeds the demo admin account so the finance screens are reachable on first launch. */
    private static void seedAdminIfNeeded() throws Exception {
        try (Connection c = getConnection()) {
            if (tableCount(c, "finance_Users") > 0) return;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO finance_Users(Email, PasswordHash) VALUES (?,?)")) {
                ps.setString(1, "admin@loop.co.uk");
                ps.setString(2, gp.loop.service.AuthService.sha256("admin123"));
                ps.executeUpdate();
            }
        }
    }

}
