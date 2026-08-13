package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import model.TransferItems;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryIntegrationTest {

    private static Path testDatabase;

    @BeforeAll
    static void prepareDatabase() throws Exception {
        Path sharedDatabase = Path.of("../../database/loop.db").toAbsolutePath().normalize();
        testDatabase = Files.createTempFile("loop-inventory-test-", ".db");
        Files.copy(sharedDatabase, testDatabase, StandardCopyOption.REPLACE_EXISTING);
        System.setProperty("loop.db.path", testDatabase.toString());
        DBConnection.verifySharedSchema();
    }

    @AfterAll
    static void removeDatabase() throws Exception {
        Files.deleteIfExists(testDatabase);
    }

    @Test
    void sharedDatabaseHasValidInventoryRelationships() throws Exception {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            assertEquals("ok", scalarString(statement, "PRAGMA integrity_check"));
            try (ResultSet foreignKeys = statement.executeQuery("PRAGMA foreign_key_check")) {
                assertFalse(foreignKeys.next());
            }
            assertEquals(0, scalarInt(statement, """
                    SELECT COUNT(*) FROM inventory_Stock stock
                    LEFT JOIN product_Ingredient ingredient
                      ON ingredient.ingredientID=stock.ingredientID
                    WHERE ingredient.ingredientID IS NULL
                    """));
        }
    }

    @Test
    void liveInventoryDaosReadNormalizedStock() throws Exception {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            int stockRows = scalarInt(statement,
                    "SELECT COUNT(*) FROM inventory_Stock WHERE stockYear=2026");
            int warehouses = scalarInt(statement, """
                    SELECT COUNT(*) FROM (
                      SELECT UPPER(TRIM(warehouseID))
                      FROM inventory_Stock
                      WHERE stockYear=2026 AND warehouseID IS NOT NULL AND TRIM(warehouseID)<>''
                      GROUP BY UPPER(TRIM(warehouseID)))
                    """);
            int ingredients = scalarInt(statement,
                    "SELECT COUNT(*) FROM product_Ingredient");

            assertEquals(stockRows, AnalyticsDAO.getLocations().size());
            assertEquals(warehouses, LocationsDAO.getLocations("live").size());
            assertEquals(ingredients, new FoodItemDAO().getAllFoodItems().size());
        }
    }

    @Test
    void transferUpdatesStockAndRollsBackFailedBatch() throws Exception {
        TransferFixture fixture = findFixture();
        TransferItems valid = transfer(
                fixture.product, fixture.sourceWarehouse, fixture.destinationWarehouse, 1);

        int beforeTotal = ingredientTotal(fixture.ingredientId);
        int beforeSource = warehouseTotal(fixture.ingredientId, fixture.sourceWarehouse);
        int beforeLogs = logCount();

        TransferItems invalid = transfer(
                fixture.product, fixture.sourceWarehouse, fixture.destinationWarehouse,
                fixture.sourceQuantity + 10_000);
        assertThrows(Exception.class,
                () -> StockTransferDAO.transferAll(List.of(valid, invalid)));
        assertEquals(beforeTotal, ingredientTotal(fixture.ingredientId));
        assertEquals(beforeSource, warehouseTotal(fixture.ingredientId, fixture.sourceWarehouse));
        assertEquals(beforeLogs, logCount());

        StockTransferDAO.transferAll(List.of(valid));
        assertEquals(beforeTotal, ingredientTotal(fixture.ingredientId));
        assertEquals(beforeSource - 1,
                warehouseTotal(fixture.ingredientId, fixture.sourceWarehouse));
        assertEquals(fixture.destinationQuantity + 1,
                warehouseTotal(fixture.ingredientId, fixture.destinationWarehouse));
        assertEquals(beforeLogs + 1, logCount());
    }

    private static TransferFixture findFixture() throws Exception {
        String sql = """
                SELECT stock.ingredientID, ingredient.ingredientName,
                       UPPER(TRIM(stock.warehouseID)) AS sourceWarehouse,
                       stock.stockQuantity
                FROM inventory_Stock stock
                JOIN product_Ingredient ingredient
                  ON ingredient.ingredientID=stock.ingredientID
                WHERE stock.stockYear=2026 AND stock.stockQuantity>=2
                ORDER BY stock.stockQuantity DESC
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                int ingredientId = result.getInt("ingredientID");
                String source = result.getString("sourceWarehouse");
                DestinationFixture destination = findDestinationWarehouse(
                        connection, ingredientId, source);
                if (destination != null) {
                    return new TransferFixture(
                            ingredientId,
                            result.getString("ingredientName"),
                            source,
                            destination.warehouse,
                            result.getInt("stockQuantity"),
                            destination.quantity);
                }
            }
        }
        throw new IllegalStateException("No transferable stock fixture found.");
    }

    private static DestinationFixture findDestinationWarehouse(
            Connection connection, int ingredientId, String source) throws Exception {
        String sql = """
                SELECT UPPER(TRIM(candidate.warehouseID)) AS warehouseID,
                       candidate.stockQuantity
                FROM inventory_Stock candidate
                WHERE candidate.stockYear=2026
                  AND candidate.ingredientID=?
                  AND UPPER(TRIM(candidate.warehouseID))<>?
                  AND candidate.stockQuantity < candidate.capacity
                ORDER BY warehouseID LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ingredientId);
            statement.setString(2, source);
            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? new DestinationFixture(result.getString(1), result.getInt(2))
                        : null;
            }
        }
    }

    private static TransferItems transfer(
            String product, String source, String destination, int quantity) {
        TransferItems item = new TransferItems();
        item.setProduct(product);
        item.setFromAddress(source);
        item.setToAddress(destination);
        item.setQuantity(quantity);
        item.setReason("Inventory integration test");
        return item;
    }

    private static int ingredientTotal(int ingredientId) throws Exception {
        return queryInt("SELECT COALESCE(SUM(stockQuantity),0) FROM inventory_Stock "
                + "WHERE stockYear=2026 AND ingredientID=?", ingredientId, null);
    }

    private static int warehouseTotal(int ingredientId, String warehouse) throws Exception {
        return queryInt("SELECT COALESCE(SUM(stockQuantity),0) FROM inventory_Stock "
                + "WHERE stockYear=2026 AND ingredientID=? AND UPPER(TRIM(warehouseID))=?",
                ingredientId, warehouse);
    }

    private static int queryInt(String sql, int ingredientId, String warehouse) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ingredientId);
            if (warehouse != null) {
                statement.setString(2, warehouse);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    private static int logCount() throws Exception {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            return scalarInt(statement,
                    "SELECT COUNT(*) FROM inventory_stock_TransactionLog");
        }
    }

    private static int scalarInt(Statement statement, String sql) throws Exception {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private static String scalarString(Statement statement, String sql) throws Exception {
        try (ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private static final class TransferFixture {
        private final int ingredientId;
        private final String product;
        private final String sourceWarehouse;
        private final String destinationWarehouse;
        private final int sourceQuantity;
        private final int destinationQuantity;

        private TransferFixture(
                int ingredientId,
                String product,
                String sourceWarehouse,
                String destinationWarehouse,
                int sourceQuantity,
                int destinationQuantity) {
            this.ingredientId = ingredientId;
            this.product = product;
            this.sourceWarehouse = sourceWarehouse;
            this.destinationWarehouse = destinationWarehouse;
            this.sourceQuantity = sourceQuantity;
            this.destinationQuantity = destinationQuantity;
        }
    }

    private static final class DestinationFixture {
        private final String warehouse;
        private final int quantity;

        private DestinationFixture(String warehouse, int quantity) {
            this.warehouse = warehouse;
            this.quantity = quantity;
        }
    }
}
