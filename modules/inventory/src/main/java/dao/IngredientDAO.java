package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.*;
import model.Ingredient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class IngredientDAO {

	public static ObservableList<Ingredient> getAllIngredients(String tableName){
		ObservableList<Ingredient> list = FXCollections.observableArrayList();
		
		String sql = "SELECT * FROM " + tableName;
		
		try(Connection conn = DBConnection.getConnectionURLlocation();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			 while (rs.next()) {
				 
				 list.add(new Ingredient(
						 rs.getString("ingredientID"),
						 rs.getString("ingredientName"),
						 rs.getInt("stockQuantity"),
						 rs.getString("warehouseID"),
						 rs.getInt("capacity")
				));
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return list;
		}
	
	
	
	public static ObservableList<String> getIngredients(String tableName) {

	    ObservableList<String> list = FXCollections.observableArrayList();

	    String sql = "SELECT ingredientName FROM " + tableName;

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

	    String sql1 =
	        "INSERT INTO inventory_stock_Ingredient2026 " +
	        "(ingredientID, ingredientName, stockQuantity, warehouseID, capacity, imagePath) " +
	        "VALUES (?,?,?,?,?,?)";


	    String sql2 =
	        "INSERT INTO inventory_product_Ingredient " +
	        "(ingredientID, ingredientName, imagePath) " +
	        "VALUES (?,?,?)";


	    try(Connection con =
	        DBConnection.getConnectionURLlocation()){


	        PreparedStatement ps1 =
	                con.prepareStatement(sql1);


	        ps1.setString(1,id);
	        ps1.setString(2,name);
	        ps1.setInt(3,quantity);
	        ps1.setString(4,warehouse);
	        ps1.setInt(5,capacity);
	        ps1.setString(6,imagePath);


	        ps1.executeUpdate();



	        PreparedStatement ps2 =
	                con.prepareStatement(sql2);


	        ps2.setString(1,id);
	        ps2.setString(2,name);
	        ps2.setString(3,imagePath);


	        ps2.executeUpdate();



	    }catch(Exception e){

	        e.printStackTrace();

	    }

	}
}