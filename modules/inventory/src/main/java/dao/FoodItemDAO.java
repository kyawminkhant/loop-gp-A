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

public class FoodItemDAO {

    
	
	
	public ObservableList<FoodItem> getAllFoodItems() {
        ObservableList<FoodItem> foodItemList = FXCollections.observableArrayList();
         
        String query = "SELECT ingredientID, ingredientName, imagePath FROM inventory_product_Ingredient";

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
         
        String ingredientQuery = "SELECT ingredientName, imagePath FROM inventory_product_Ingredient";

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
         
        String query = "SELECT ingredientName FROM inventory_product_Ingredient";

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
        String query = "SELECT ingredientID, ingredientName, imagePath FROM inventory_product_Ingredient WHERE ingredientID = ? OR ingredientName = ?";
        
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

}