package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.FoodItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Year;

public class FoodItemDAO {

    
	
	
	public ObservableList<FoodItem> getAllFoodItems() {
        ObservableList<FoodItem> foodItemList = FXCollections.observableArrayList();
         
        String query = "SELECT ingredientID, ingredientName, imagePath FROM product_Ingredient";

        try (Connection connection = DBConnection.getConnectionURLProduct(); 
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) { 
                int id = resultSet.getInt("ingredientID");
                String name = resultSet.getString("ingredientName");
                String URLPath = resultSet.getString("imagePath");

                FoodItem item = new FoodItem(id, name, URLPath);
                foodItemList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foodItemList;
    }
   
    
    
    
    public static ObservableList<FoodItem> getAllFoodIngredients() {
        ObservableList<FoodItem> ingredientItemList = FXCollections.observableArrayList();
         
        String ingredientQuery = "SELECT ingredientName, imagePath FROM product_Ingredient";

        try (Connection connection = DBConnection.getConnectionURLProduct(); 
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(ingredientQuery)) {

            while (resultSet.next()) { 
                String name = resultSet.getString("ingredientName");
                String URLPath = resultSet.getString("imagePath");

                FoodItem item = new FoodItem(name, URLPath);
                ingredientItemList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredientItemList;
    }

    
    
    public static ObservableList<String> getAllFoodIngredientNames() {
        ObservableList<String> ingredientNames = FXCollections.observableArrayList();
         
        String query = "SELECT ingredientName FROM product_Ingredient";

        try (Connection connection = DBConnection.getConnectionURLProduct(); 
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) { 
                ingredientNames.add(resultSet.getString("ingredientName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ingredientNames;
    }
    
    
    public static FoodItem getProductByIdOrName(String queryValue) {
        String query = "SELECT ingredientID, ingredientName, imagePath FROM product_Ingredient WHERE ingredientID = ? OR ingredientName = ?";
        
        try (Connection connection = DBConnection.getConnectionURLProduct();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            try {
                int id = Integer.parseInt(queryValue);
                preparedStatement.setInt(1, id);
            } catch (NumberFormatException e) {
                preparedStatement.setInt(1, -1); 
            }
            
            preparedStatement.setString(2, queryValue);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("ingredientID");
                    String name = resultSet.getString("ingredientName");
                    String URLPath = resultSet.getString("imagePath");
                    return new FoodItem(id, name, URLPath);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    public static int getCurrentStockForIngredient(int ingredientId) {
        String sql = "SELECT COALESCE(SUM(stockQuantity), 0) FROM inventory_Stock "
                + "WHERE stockYear = 2026 AND ingredientID = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ingredientId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load ingredient stock.", exception);
        }
    }

    public static int getStockForIngredientAtWarehouse(
            int ingredientId, String warehouseId) {
        return readWarehouseValue(ingredientId, warehouseId, "stockQuantity");
    }

    public static int getCapacityForIngredientAtWarehouse(
            int ingredientId, String warehouseId) {
        return readWarehouseValue(ingredientId, warehouseId, "capacity");
    }

    private static int readWarehouseValue(
            int ingredientId, String warehouseId, String column) {
        if (!"stockQuantity".equals(column) && !"capacity".equals(column)) {
            throw new IllegalArgumentException("Unsupported stock column.");
        }
        String sql = "SELECT " + column + " FROM inventory_Stock "
                + "WHERE stockYear=? AND ingredientID=? "
                + "AND UPPER(TRIM(warehouseID))=? LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Year.now().getValue());
            statement.setInt(2, ingredientId);
            statement.setString(3, normaliseWarehouse(warehouseId));
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load warehouse stock.", exception);
        }
    }

    public static void updateStockForIngredientAtWarehouse(
            int ingredientId, String warehouseId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        String warehouse = normaliseWarehouse(warehouseId);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String stockCode;
                int capacity;
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT stockCode, capacity FROM inventory_Stock
                        WHERE stockYear=? AND ingredientID=?
                          AND UPPER(TRIM(warehouseID))=? LIMIT 1
                        """)) {
                    statement.setInt(1, Year.now().getValue());
                    statement.setInt(2, ingredientId);
                    statement.setString(3, warehouse);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Warehouse stock row was not found.");
                        }
                        stockCode = result.getString("stockCode");
                        capacity = result.getInt("capacity");
                    }
                }
                if (quantity > capacity) {
                    throw new IllegalArgumentException(
                            "Stock cannot exceed the warehouse capacity of " + capacity + ".");
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE inventory_Stock SET stockQuantity=?
                        WHERE stockYear=? AND stockCode=?
                        """)) {
                    statement.setInt(1, quantity);
                    statement.setInt(2, Year.now().getValue());
                    statement.setString(3, stockCode);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO inventory_stock_TransactionLog
                          (username, action, details, dateTime)
                        VALUES ('Inventory User', 'LOCATION_STOCK_UPDATE', ?, datetime('now'))
                        """)) {
                    statement.setString(1, "Ingredient #" + ingredientId + " set to "
                            + quantity + " units at " + warehouse);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                }
                throw new IllegalStateException("Could not update warehouse stock.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update warehouse stock.", exception);
        }
    }

    private static String normaliseWarehouse(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

}
