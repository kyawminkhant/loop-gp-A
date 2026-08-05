package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import Utils.Session;
import model.TransferItems;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Applies warehouse transfers to inventory_Stock as one atomic transaction. */
public final class StockTransferDAO {

    private static final int CURRENT_STOCK_YEAR = 2026;
    private static final DateTimeFormatter LOG_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StockTransferDAO() { }

    public static void transferAll(List<TransferItems> transfers) throws SQLException {
        if (transfers == null || transfers.isEmpty()) {
            throw new SQLException("Add at least one stock transfer.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (TransferItems transfer : transfers) {
                    transferOne(connection, transfer);
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new SQLException("Could not complete the stock transfer.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void transferOne(Connection connection, TransferItems transfer)
            throws SQLException {
        validate(transfer);
        String from = normaliseWarehouse(transfer.getFromAddress());
        String to = normaliseWarehouse(transfer.getToAddress());

        int ingredientId = findIngredientId(connection, transfer.getProduct());
        SourceStock source = findSourceStock(
                connection, ingredientId, from, transfer.getQuantity());

        DestinationStock destination = findDestinationStock(connection, ingredientId, to);
        if (destination == null) {
            insertDestination(connection, ingredientId, to, transfer.getQuantity(), source);
        } else {
            int newQuantity = destination.quantity + transfer.getQuantity();
            if (newQuantity > destination.capacity) {
                throw new SQLException("Destination " + to + " does not have enough capacity for "
                        + transfer.getProduct() + ".");
            }
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE inventory_Stock SET stockQuantity=?
                    WHERE stockYear=? AND stockCode=?
                    """)) {
                statement.setInt(1, newQuantity);
                statement.setInt(2, CURRENT_STOCK_YEAR);
                statement.setString(3, destination.stockCode);
                statement.executeUpdate();
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE inventory_Stock SET stockQuantity=stockQuantity-?
                WHERE stockYear=? AND stockCode=?
                """)) {
            statement.setInt(1, transfer.getQuantity());
            statement.setInt(2, CURRENT_STOCK_YEAR);
            statement.setString(3, source.stockCode);
            statement.executeUpdate();
        }

        insertAuditLog(connection, transfer, from, to);
    }

    private static void validate(TransferItems transfer) throws SQLException {
        if (transfer == null
                || isBlank(transfer.getFromAddress())
                || isBlank(transfer.getToAddress())
                || isBlank(transfer.getProduct())) {
            throw new SQLException("Select a source, destination, and ingredient for every transfer.");
        }
        if (transfer.getQuantity() <= 0) {
            throw new SQLException("Transfer quantity must be greater than zero.");
        }
        if (normaliseWarehouse(transfer.getFromAddress())
                .equals(normaliseWarehouse(transfer.getToAddress()))) {
            throw new SQLException("Source and destination warehouses must be different.");
        }
    }

    private static int findIngredientId(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT ingredientID FROM product_Ingredient
                WHERE lower(trim(ingredientName))=lower(trim(?)) LIMIT 1
                """)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }
        throw new SQLException("Ingredient is no longer available: " + name);
    }

    private static SourceStock findSourceStock(
            Connection connection, int ingredientId, String warehouse, int quantity)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT stockCode, stockQuantity, capacity
                FROM inventory_Stock
                WHERE stockYear=? AND ingredientID=?
                  AND UPPER(TRIM(warehouseID))=? AND stockQuantity>=?
                ORDER BY stockQuantity DESC LIMIT 1
                """)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            statement.setInt(2, ingredientId);
            statement.setString(3, warehouse);
            statement.setInt(4, quantity);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new SourceStock(
                            result.getString("stockCode"),
                            result.getInt("capacity"));
                }
            }
        }
        throw new SQLException("Not enough stock is available at " + warehouse + ".");
    }

    private static DestinationStock findDestinationStock(
            Connection connection, int ingredientId, String warehouse) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT stockCode, stockQuantity, capacity
                FROM inventory_Stock
                WHERE stockYear=? AND ingredientID=? AND UPPER(TRIM(warehouseID))=?
                ORDER BY stockQuantity DESC LIMIT 1
                """)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            statement.setInt(2, ingredientId);
            statement.setString(3, warehouse);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new DestinationStock(
                            result.getString("stockCode"),
                            result.getInt("stockQuantity"),
                            result.getInt("capacity"));
                }
            }
        }
        return null;
    }

    private static void insertDestination(
            Connection connection,
            int ingredientId,
            String warehouse,
            int quantity,
            SourceStock source) throws SQLException {
        String stockCode = nextStockCode(connection, source.stockCode, warehouse);
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO inventory_Stock
                  (stockYear, stockCode, ingredientID, stockQuantity, warehouseID, capacity)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            statement.setString(2, stockCode);
            statement.setInt(3, ingredientId);
            statement.setInt(4, quantity);
            statement.setString(5, warehouse);
            statement.setInt(6, Math.max(quantity, source.capacity));
            statement.executeUpdate();
        }
    }

    private static String nextStockCode(
            Connection connection, String sourceCode, String warehouse) throws SQLException {
        String base = sourceCode + "-" + warehouse.replaceAll("[^A-Z0-9]", "");
        String candidate = base;
        int suffix = 2;
        while (stockCodeExists(connection, candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private static boolean stockCodeExists(Connection connection, String code) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT 1 FROM inventory_Stock WHERE stockYear=? AND stockCode=?
                """)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            statement.setString(2, code);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static void insertAuditLog(
            Connection connection, TransferItems transfer, String from, String to)
            throws SQLException {
        String username = Session.getUser() == null ? "Inventory User"
                : Session.getUser().getUsername();
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO inventory_stock_TransactionLog
                  (username, action, details, dateTime)
                VALUES (?, 'TRANSFER', ?, ?)
                """)) {
            statement.setString(1, username);
            statement.setString(2, transfer.getQuantity() + " x " + transfer.getProduct()
                    + " from " + from + " to " + to
                    + (isBlank(transfer.getReason()) ? "" : " (" + transfer.getReason().trim() + ")"));
            statement.setString(3, LocalDateTime.now().format(LOG_TIME));
            statement.executeUpdate();
        }
    }

    private static String normaliseWarehouse(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class SourceStock {
        private final String stockCode;
        private final int capacity;

        private SourceStock(String stockCode, int capacity) {
            this.stockCode = stockCode;
            this.capacity = capacity;
        }
    }

    private static final class DestinationStock {
        private final String stockCode;
        private final int quantity;
        private final int capacity;

        private DestinationStock(String stockCode, int quantity, int capacity) {
            this.stockCode = stockCode;
            this.quantity = quantity;
            this.capacity = capacity;
        }
    }
}
