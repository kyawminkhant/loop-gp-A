package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.StorageLocations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Reads only warehouse locations that are actually used by live stock. */
public final class LocationsDAO {

    private static final int CURRENT_STOCK_YEAR = 2026;

    private LocationsDAO() { }

    public static ObservableList<StorageLocations> getStorageLocations(String ignoredTableName) {
        ObservableList<StorageLocations> list = FXCollections.observableArrayList();
        try (Connection connection = DBConnection.getConnection()) {
            boolean hasWarehouseDirectory = tableExists(
                    connection, "inventory_Warehouses");
            String warehouseName = hasWarehouseDirectory
                    ? "COALESCE(warehouse.name, 'Warehouse ' || "
                      + "UPPER(TRIM(stock.warehouseID)))"
                    : "'Warehouse ' || UPPER(TRIM(stock.warehouseID))";
            String serviceArea = hasWarehouseDirectory
                    ? "COALESCE(warehouse.serviceArea, UPPER(TRIM(stock.warehouseID)))"
                    : "UPPER(TRIM(stock.warehouseID))";
            String warehouseJoin = hasWarehouseDirectory
                    ? "LEFT JOIN inventory_Warehouses warehouse "
                      + "ON warehouse.warehouseID = UPPER(TRIM(stock.warehouseID))"
                    : "";
            String groupColumns = hasWarehouseDirectory
                    ? ", warehouse.name, warehouse.serviceArea" : "";
            String sql = """
                SELECT UPPER(TRIM(stock.warehouseID)) AS warehouseID,
                       %s AS warehouseName,
                       %s AS serviceArea,
                       SUM(stock.capacity) AS capacity,
                       SUM(stock.stockQuantity) AS currentStock
                FROM inventory_Stock stock
                %s
                WHERE stock.stockYear = ?
                  AND stock.warehouseID IS NOT NULL
                  AND TRIM(stock.warehouseID) <> ''
                GROUP BY UPPER(TRIM(stock.warehouseID)) %s
                ORDER BY UPPER(TRIM(stock.warehouseID))
                """.formatted(
                        warehouseName, serviceArea, warehouseJoin, groupColumns);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, CURRENT_STOCK_YEAR);
                try (ResultSet result = statement.executeQuery()) {
                    int locationId = 1;
                    while (result.next()) {
                        String warehouse = result.getString("warehouseID");
                        list.add(new StorageLocations(
                                result.getString("warehouseName"),
                                result.getString("serviceArea") + " (" + warehouse + ")",
                                locationId++,
                                result.getInt("capacity"),
                                result.getInt("currentStock"),
                                "Inventory Team"));
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load live warehouse totals.", exception);
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

    public static ObservableList<String> getLocations(String ignoredTableName) {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (StorageLocations location : getStorageLocations(ignoredTableName)) {
            list.add(location.getLocations());
        }
        return list;
    }
}
