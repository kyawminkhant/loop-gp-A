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
        assertEquals(1,
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
                String destination = findUnusedWarehouse(connection, ingredientId, source);
                if (destination != null) {
                    return new TransferFixture(
                            ingredientId,
                            result.getString("ingredientName"),
                            source,
                            destination,
                            result.getInt("stockQuantity"));
                }
            }
        }
        throw new IllegalStateException("No transferable stock fixture found.");
    }

    private static String findUnusedWarehouse(
            Connection connection, int ingredientId, String source) throws Exception {
        String sql = """
                SELECT DISTINCT UPPER(TRIM(candidate.warehouseID)) AS warehouseID
                FROM inventory_Stock candidate
                WHERE candidate.stockYear=2026
                  AND UPPER(TRIM(candidate.warehouseID))<>?
                  AND NOT EXISTS (
                    SELECT 1 FROM inventory_Stock existing
                    WHERE existing.stockYear=2026 AND existing.ingredientID=?
                      AND UPPER(TRIM(existing.warehouseID))=UPPER(TRIM(candidate.warehouseID)))
                ORDER BY warehouseID LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, source);
            statement.setInt(2, ingredientId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
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

        private TransferFixture(
                int ingredientId,
                String product,
                String sourceWarehouse,
                String destinationWarehouse,
                int sourceQuantity) {
            this.ingredientId = ingredientId;
            this.product = product;
            this.sourceWarehouse = sourceWarehouse;
            this.destinationWarehouse = destinationWarehouse;
            this.sourceQuantity = sourceQuantity;
        }
    }
}
