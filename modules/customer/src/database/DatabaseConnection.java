package database;

import java.sql.*;
import java.util.UUID;

public class DatabaseConnection {

    private static final String MAIN_DB_URL = "jdbc:sqlite:" +
            System.getProperty("loop.db.path", "database/loop.db");
    private static String dbUrl = MAIN_DB_URL;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    /** Used by JUnit tests so the main database is not modified. */
    public static void useTestDatabase() {
        dbUrl = "jdbc:sqlite:loop_customers_test.db";
    }

    public static void useMainDatabase() {
        dbUrl = MAIN_DB_URL;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_People (
                    personID TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    mobile TEXT NOT NULL,
                    passwordHash TEXT NOT NULL
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_Customers (
                    customerID TEXT PRIMARY KEY,
                    personID TEXT NOT NULL,
                    deliveryAddress TEXT NOT NULL,
                    idCardNo TEXT UNIQUE NOT NULL,
                    idCardImagePath TEXT,
                    status TEXT NOT NULL DEFAULT 'Active',
                    FOREIGN KEY (personID) REFERENCES customer_People(personID)
                );""");

            ensureColumnExists(conn, "customer_Customers", "idCardImagePath", "TEXT");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_CustomerPreference (
                    preferenceID TEXT PRIMARY KEY,
                    customerID TEXT NOT NULL,
                    favoriteCategories TEXT,
                    notificationSettings TEXT,
                    deliveryInstructions TEXT,
                    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID)
                );""");

            stmt.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS customer_UQ_CustomerPreference_Customer
                ON customer_CustomerPreference(customerID);""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders_Orders (
                    orderID INTEGER PRIMARY KEY AUTOINCREMENT,
                    customerID TEXT,
                    customerName TEXT NOT NULL DEFAULT 'Guest',
                    orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status TEXT NOT NULL DEFAULT 'Pending',
                    totalAmount REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID)
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_Chefs (
                    chefID TEXT PRIMARY KEY,
                    chefName TEXT NOT NULL,
                    speciality TEXT,
                    averageRating REAL DEFAULT 0
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_ChefReviews (
                    reviewID TEXT PRIMARY KEY,
                    customerID TEXT NOT NULL,
                    chefID TEXT NOT NULL,
                    rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
                    reviewText TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID),
                    FOREIGN KEY (chefID) REFERENCES customer_Chefs(chefID),
                    UNIQUE(customerID, chefID)
                );""");

            seedChefs(conn);
            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureColumnExists(Connection conn, String table, String column, String type)
            throws SQLException {
        boolean found = false;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            }
        }
    }

    private static void seedChefs(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customer_Chefs")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String[][] chefs = {
            {"Gordon Ramsay",     "Fine Dining & British Cuisine",  "4.8"},
            {"Jamie Oliver",      "Italian & Mediterranean",         "4.6"},
            {"Yotam Ottolenghi", "Middle Eastern & Vegetarian",    "4.7"},
            {"Nigella Lawson",   "Comfort Food & Baking",          "4.5"},
            {"Heston Blumenthal","Molecular Gastronomy",            "4.9"},
            {"Rick Stein",       "Seafood & Fish Dishes",          "4.4"},
            {"Ainsley Harriott", "Caribbean & Fusion",              "4.3"},
            {"Nadiya Hussain",   "South Asian & Baking",           "4.6"},
        };

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customer_Chefs (chefID, chefName, speciality, averageRating) VALUES (?, ?, ?, ?)")) {
            for (String[] c : chefs) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, c[0]);
                ps.setString(3, c[1]);
                ps.setDouble(4, Double.parseDouble(c[2]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
