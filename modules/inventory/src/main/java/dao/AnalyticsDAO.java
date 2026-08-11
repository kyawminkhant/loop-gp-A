package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Analytics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Builds analytics from the live normalized stock table. */
public final class AnalyticsDAO {

    private static final int CURRENT_STOCK_YEAR = 2026;

    private AnalyticsDAO() { }

    public static ObservableList<Analytics> getLocations() {
        ObservableList<Analytics> list = FXCollections.observableArrayList();
        try (Connection connection = DBConnection.getConnection()) {
            boolean hasWarehouseDirectory = tableExists(
                    connection, "inventory_Warehouses");
            String locationColumn = hasWarehouseDirectory
                    ? "COALESCE(warehouse.serviceArea, stock.warehouseID) "
                      + "|| ' (' || stock.warehouseID || ')'"
                    : "stock.warehouseID";
            String warehouseJoin = hasWarehouseDirectory
                    ? "LEFT JOIN inventory_Warehouses warehouse "
                      + "ON warehouse.warehouseID = UPPER(TRIM(stock.warehouseID))"
                    : "";
            String sql = """
                SELECT ingredient.ingredientName AS product,
                       %s AS location,
                       stock.stockQuantity AS currentQuantity,
                       MAX(1, CAST(stock.capacity * 0.20 AS INTEGER)) AS minThreshold,
                       CASE
                         WHEN stock.stockQuantity <= MAX(1, CAST(stock.capacity * 0.10 AS INTEGER))
                           THEN 'CRITICAL'
                         WHEN stock.stockQuantity <= MAX(1, CAST(stock.capacity * 0.20 AS INTEGER))
                           THEN 'LOW'
                         ELSE 'AVAILABLE'
                       END AS status,
                       COALESCE(legacy.lastRestock, 'Not recorded') AS lastRestock,
                       CASE
                         WHEN stock.stockQuantity <= MAX(1, CAST(stock.capacity * 0.20 AS INTEGER))
                           THEN 0
                         ELSE CAST((stock.stockQuantity - MAX(1, CAST(stock.capacity * 0.20 AS INTEGER)))
                              / MAX(1, stock.capacity / 30) AS INTEGER)
                       END AS daysUntilReorder
                FROM inventory_Stock stock
                JOIN product_Ingredient ingredient
                  ON ingredient.ingredientID = stock.ingredientID
                %s
                LEFT JOIN inventory_stock_analytics legacy
                  ON legacy.product = ingredient.ingredientName
                WHERE stock.stockYear = ?
                ORDER BY CASE status WHEN 'CRITICAL' THEN 0 WHEN 'LOW' THEN 1 ELSE 2 END,
                         ingredient.ingredientName, stock.warehouseID
                """.formatted(locationColumn, warehouseJoin);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, CURRENT_STOCK_YEAR);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        list.add(new Analytics(
                                result.getString("product"),
                                result.getString("location"),
                                result.getInt("currentQuantity"),
                                result.getInt("minThreshold"),
                                result.getString("status"),
                                result.getString("lastRestock"),
                                result.getInt("daysUntilReorder")));
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load live inventory analytics.", exception);
        }
        return list;
    }

    private static boolean tableExists(Connection connection, String table)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            statement.setString(1, table);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
