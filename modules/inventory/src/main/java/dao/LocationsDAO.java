package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.StorageLocations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Reads only warehouse locations that are actually used by live stock. */
public final class LocationsDAO {

    private static final int CURRENT_STOCK_YEAR = 2026;

    private LocationsDAO() { }

    public static ObservableList<StorageLocations> getStorageLocations(String ignoredTableName) {
        ObservableList<StorageLocations> list = FXCollections.observableArrayList();
        String sql = """
                SELECT UPPER(TRIM(warehouseID)) AS warehouseID,
                       SUM(capacity) AS capacity,
                       SUM(stockQuantity) AS currentStock
                FROM inventory_Stock
                WHERE stockYear = ? AND warehouseID IS NOT NULL AND TRIM(warehouseID) <> ''
                GROUP BY UPPER(TRIM(warehouseID))
                ORDER BY UPPER(TRIM(warehouseID))
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            try (ResultSet result = statement.executeQuery()) {
                int locationId = 1;
                while (result.next()) {
                    String warehouse = result.getString("warehouseID");
                    list.add(new StorageLocations(
                            "Warehouse " + warehouse,
                            warehouse,
                            locationId++,
                            result.getInt("capacity"),
                            result.getInt("currentStock"),
                            "Inventory Team"));
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load live warehouse totals.", exception);
        }
        return list;
    }

    public static ObservableList<String> getLocations(String ignoredTableName) {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (StorageLocations location : getStorageLocations(ignoredTableName)) {
            list.add(location.getLocations());
        }
        return list;
    }
}
