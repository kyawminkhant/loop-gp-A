package gp.loop.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/** Shared SQLite access for Finance reporting and its administrator account. */
public final class Database {

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
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE.toAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initialize() throws Exception {
        Path parent = DB_FILE.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            createSharedTables(statement);
            createFinanceViews(statement);
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS finance_Users ("
                            + "UserID INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "Email TEXT UNIQUE, PasswordHash TEXT)");
        }

        seedDefaultChefIfNeeded();
        seedIfEmpty();
        seedAdminIfNeeded();
        System.out.println("Database initialized at " + filePath());
    }

    private static void createSharedTables(Statement statement) throws Exception {
        statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS product_Chef ("
                        + "chefID INTEGER PRIMARY KEY AUTOINCREMENT, chefName TEXT NOT NULL, "
                        + "\"chefRating&ReviewID\" INTEGER NOT NULL DEFAULT 0, "
                        + "chefDescription TEXT NOT NULL DEFAULT '', chefTag1 TEXT NOT NULL DEFAULT '', "
                        + "chefTag2 TEXT NOT NULL DEFAULT '', chefTag3 TEXT NOT NULL DEFAULT '', "
                        + "chefImage TEXT NOT NULL DEFAULT '', chefEmail TEXT NOT NULL DEFAULT '', "
                        + "chefTel TEXT NOT NULL DEFAULT '')");
        statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS product_Products ("
                        + "productID INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT NOT NULL, "
                        + "shortDescription TEXT NOT NULL, extendedDescription TEXT NOT NULL, "
                        + "cost REAL NOT NULL, price REAL NOT NULL, status INTEGER NOT NULL DEFAULT 1, "
                        + "spiceLevel INTEGER NOT NULL DEFAULT 0, country TEXT NOT NULL, "
                        + "createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "updatedDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, chefID INTEGER NOT NULL, "
                        + "sourceModule TEXT NOT NULL DEFAULT 'product', "
                        + "stockQuantity INTEGER NOT NULL DEFAULT 100, "
                        + "FOREIGN KEY (chefID) REFERENCES product_Chef(chefID))");
        statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS orders_Orders ("
                        + "orderID INTEGER PRIMARY KEY AUTOINCREMENT, customerID TEXT, "
                        + "customerName TEXT NOT NULL DEFAULT 'Guest', "
                        + "orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "status TEXT NOT NULL DEFAULT 'Pending', totalAmount REAL NOT NULL DEFAULT 0)");
        statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS orders_OrderItems ("
                        + "orderItemID INTEGER PRIMARY KEY AUTOINCREMENT, orderID INTEGER NOT NULL, "
                        + "productID INTEGER NOT NULL, itemName TEXT NOT NULL, "
                        + "quantity INTEGER NOT NULL DEFAULT 1, priceAtOrder REAL NOT NULL DEFAULT 0, "
                        + "FOREIGN KEY (orderID) REFERENCES orders_Orders(orderID) ON DELETE CASCADE, "
                        + "FOREIGN KEY (productID) REFERENCES product_Products(productID))");
    }

    private static void createFinanceViews(Statement statement) throws Exception {
        statement.executeUpdate(
                "CREATE VIEW IF NOT EXISTS finance_Orders AS "
                        + "SELECT orderID AS OrderID, orderDate AS Date, totalAmount AS TotalCost "
                        + "FROM orders_Orders");
        statement.executeUpdate(
                "CREATE VIEW IF NOT EXISTS finance_OrderItem AS "
                        + "SELECT orderItemID AS OrderItemID, orderID AS OrderID, productID AS ProductID, "
                        + "quantity AS Quantity, priceAtOrder AS UnitPrice FROM orders_OrderItems");
        statement.executeUpdate(
                "CREATE VIEW IF NOT EXISTS finance_Product AS "
                        + "SELECT productID AS ProductID, productName AS Name, price AS Price, cost "
                        + "FROM product_Products");
    }

    private static void seedIfEmpty() throws Exception {
        try (Connection connection = getConnection()) {
            if (tableCount(connection, "orders_Orders") == 0) {
                seedDemoOrder(connection, "Weekly box", 45.99, 27.25, "2026-03-03T10:00:00");
                seedDemoOrder(connection, "Weekly box", 45.99, 27.25, "2026-03-10T10:00:00");
                seedDemoOrder(connection, "Add-on snacks", 12.99, 6.00, "2026-03-11T18:22:00");
            }
        }
    }

    private static int tableCount(Connection connection, String table) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) AS n FROM " + table)) {
            return result.next() ? result.getInt("n") : 0;
        }
    }

    private static void seedDemoOrder(
            Connection connection, String productName, double unitPrice, double unitCost, String when)
            throws Exception {
        connection.setAutoCommit(false);
        try {
            long productId = findOrCreateFinanceProduct(connection, productName, unitPrice, unitCost);

            long orderId;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO orders_Orders(customerName, orderDate, status, totalAmount) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, "Finance demo");
                statement.setString(2, when);
                statement.setString(3, "Delivered");
                statement.setDouble(4, unitPrice);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    orderId = keys.getLong(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO orders_OrderItems(orderID, productID, itemName, quantity, priceAtOrder) "
                            + "VALUES (?,?,?,?,?)")) {
                statement.setLong(1, orderId);
                statement.setLong(2, productId);
                statement.setString(3, productName);
                statement.setInt(4, 1);
                statement.setDouble(5, unitPrice);
                statement.executeUpdate();
            }

            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static void addDemoOrder() throws Exception {
        try (Connection connection = getConnection()) {
            seedDemoOrder(connection, "Weekly box", 45.99, 27.25,
                    java.time.LocalDateTime.now().toString());
        }
    }

    private static long findOrCreateFinanceProduct(
            Connection connection, String name, double price, double cost) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT productID FROM product_Products "
                        + "WHERE productName = ? AND sourceModule = 'finance' LIMIT 1")) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong(1);
                }
            }
        }

        long chefId;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT MIN(chefID) FROM product_Chef")) {
            result.next();
            chefId = result.getLong(1);
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO product_Products "
                        + "(productName, shortDescription, extendedDescription, cost, price, status, "
                        + "spiceLevel, country, chefID, sourceModule, stockQuantity) "
                        + "VALUES (?, 'Finance catalogue item', 'Finance reporting demo item', ?, ?, "
                        + "0, 0, 'United Kingdom', ?, 'finance', 0)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setDouble(2, cost);
            statement.setDouble(3, price);
            statement.setLong(4, chefId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private static void seedDefaultChefIfNeeded() throws Exception {
        try (Connection connection = getConnection()) {
            if (tableCount(connection, "product_Chef") > 0) {
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO product_Chef "
                            + "(chefName, \"chefRating&ReviewID\", chefDescription, chefTag1, chefTag2, "
                            + "chefTag3, chefImage, chefEmail, chefTel) VALUES "
                            + "('LOOP Finance', 0, 'Finance demo chef', '', '', '', '', 'finance@loop.com', '')")) {
                statement.executeUpdate();
            }
        }
    }

    /** Seeds the demo admin account so the finance screens are reachable on first launch. */
    private static void seedAdminIfNeeded() throws Exception {
        try (Connection connection = getConnection()) {
            if (tableCount(connection, "finance_Users") > 0) {
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO finance_Users(Email, PasswordHash) VALUES (?,?)")) {
                statement.setString(1, "admin@loop.co.uk");
                statement.setString(2, gp.loop.service.AuthService.sha256("admin123"));
                statement.executeUpdate();
            }
        }
    }
}
