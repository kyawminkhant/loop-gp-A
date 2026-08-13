package LoopsFirstYearProject.LoopsFirstYearProject.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

/** Central connection point for Inventory's part of the shared LOOP database. */
public final class DBConnection {

    private static final Path SHARED_DATABASE = resolveSharedDatabasePath();
    private static final String SHARED_URL = "jdbc:sqlite:" + SHARED_DATABASE;
    private static final Set<String> REQUIRED_TABLES = Set.of(
            "product_Ingredient",
            "inventory_Stock",
            "inventory_stock_TransactionLog");

    private DBConnection() { }

    /**
     * Resolves the shared database both from the repository root and when the
     * Inventory project is launched directly from Eclipse. Maven supplies
     * loop.db.path explicitly; Eclipse commonly uses modules/inventory as its
     * working directory, so ../../database/loop.db is the required fallback.
     */
    private static Path resolveSharedDatabasePath() {
        String configuredPath = System.getProperty("loop.db.path");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return normalise(Paths.get(configuredPath));
        }

        Path[] candidates = {
                Paths.get("..", "..", "database", "loop.db"),
                Paths.get("..", "database", "loop.db"),
                Paths.get("database", "loop.db")
        };
        for (Path candidate : candidates) {
            Path resolved = normalise(candidate);
            if (Files.isRegularFile(resolved)) {
                return resolved;
            }
        }
        return normalise(Paths.get("database", "loop.db"));
    }

    private static Path normalise(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /** Opens the one database used by Product and Inventory. */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(SHARED_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    /** Retained for the original DAO call sites; both aliases use the same DB. */
    public static Connection getConnectionURLProduct() throws SQLException {
        return getConnection();
    }

    /** Retained for the original DAO call sites; both aliases use the same DB. */
    public static Connection getConnectionURLlocation() throws SQLException {
        return getConnection();
    }

    public static Path getDatabasePath() {
        return SHARED_DATABASE;
    }

    /** Fails fast if Inventory is accidentally launched against an empty DB. */
    public static void verifySharedSchema() throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String table : REQUIRED_TABLES) {
                statement.setString(1, table);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException("Missing shared table " + table
                                + " in " + SHARED_DATABASE);
                    }
                }
            }
        }
    }

    public static void testConnection1() {
        testConnection();
    }

    public static void testConnection2() {
        testConnection();
    }

    public static void testConnection() {
        try {
            verifySharedSchema();
            System.out.println("Inventory connected to shared database: " + SHARED_DATABASE);
        } catch (SQLException exception) {
            System.err.println("Inventory database connection failed: " + SHARED_DATABASE);
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}
