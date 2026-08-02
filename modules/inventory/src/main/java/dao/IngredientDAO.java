package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.*;
import model.Ingredient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class IngredientDAO {

	public static ObservableList<Ingredient> getAllIngredients(String tableName){
		ObservableList<Ingredient> list = FXCollections.observableArrayList();
		int stockYear = parseStockYear(tableName);
		String sql = "SELECT stock.stockCode AS ingredientID, ingredient.ingredientName, "
				+ "stock.stockQuantity, stock.warehouseID, stock.capacity "
				+ "FROM inventory_Stock stock "
				+ "JOIN product_Ingredient ingredient ON ingredient.ingredientID = stock.ingredientID "
				+ "WHERE stock.stockYear = ? ORDER BY ingredient.ingredientName";
		
		try(Connection conn = DBConnection.getConnectionURLlocation();
			PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, stockYear);
			try (ResultSet rs = stmt.executeQuery()) {
			 while (rs.next()) {
				 
				 list.add(new Ingredient(
						 rs.getString("ingredientID"),
						 rs.getString("ingredientName"),
						 rs.getInt("stockQuantity"),
						 rs.getString("warehouseID"),
						 rs.getInt("capacity")
					 ));
			}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return list;
		}
	
	
	
	public static ObservableList<String> getIngredients(String tableName) {

	    ObservableList<String> list = FXCollections.observableArrayList();

	    String sql = "SELECT ingredientName FROM product_Ingredient ORDER BY ingredientName";

	    try (Connection conn = DBConnection.getConnectionURLProduct();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            list.add(rs.getString("ingredientName"));
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return list;
	}
	
	public static void addIngredient(
	        String id,
	        String name,
	        int quantity,
	        String warehouse,
	        int capacity,
	        String imagePath
	){

	    String insertIngredient = "INSERT INTO product_Ingredient "
	            + "(ingredientName, calories, protein, carbohydrates, sugars, fat, saturatedFat, fiber, sodium, imagePath) "
	            + "SELECT ?, 0, 0, 0, 0, 0, 0, 0, 0, ? "
	            + "WHERE NOT EXISTS (SELECT 1 FROM product_Ingredient WHERE lower(trim(ingredientName)) = lower(trim(?)))";
	    String findIngredient = "SELECT ingredientID FROM product_Ingredient "
	            + "WHERE lower(trim(ingredientName)) = lower(trim(?)) LIMIT 1";
	    String insertStock = "INSERT INTO inventory_Stock "
	            + "(stockYear, stockCode, ingredientID, stockQuantity, warehouseID, capacity) "
	            + "VALUES (2026, ?, ?, ?, ?, ?)";

	    try(Connection con = DBConnection.getConnectionURLlocation()){
	        con.setAutoCommit(false);
	        try {
	            try (PreparedStatement ps = con.prepareStatement(insertIngredient)) {
	                ps.setString(1, name);
	                ps.setString(2, imagePath);
	                ps.setString(3, name);
	                ps.executeUpdate();
	            }

	            int ingredientId;
	            try (PreparedStatement ps = con.prepareStatement(findIngredient)) {
	                ps.setString(1, name);
	                try (ResultSet rs = ps.executeQuery()) {
	                    if (!rs.next()) {
	                        throw new SQLException("Could not create the shared ingredient.");
	                    }
	                    ingredientId = rs.getInt("ingredientID");
	                }
	            }

	            try (PreparedStatement ps = con.prepareStatement(insertStock)) {
	                ps.setString(1, id);
	                ps.setInt(2, ingredientId);
	                ps.setInt(3, quantity);
	                ps.setString(4, warehouse);
	                ps.setInt(5, capacity);
	                ps.executeUpdate();
	            }
	            con.commit();
	        } catch (Exception exception) {
	            con.rollback();
	            throw exception;
	        } finally {
	            con.setAutoCommit(true);
	        }
	    }catch(Exception e){
	        e.printStackTrace();
	    }

	}

	private static int parseStockYear(String value) {
		if (value != null) {
			String digits = value.replaceAll("\\D", "");
			if (!digits.isBlank()) {
				try {
					return Integer.parseInt(digits);
				} catch (NumberFormatException ignored) {
					// Use the current project dataset below.
				}
			}
		}
		return 2026;
	}
}
