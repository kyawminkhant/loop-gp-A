package ProductPage.ProductPage;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorCompletionService;

public class DatabaseController {
	public static ArrayList<ArrayList<String>> getData(String tb) throws ClassNotFoundException, SQLException {
		String path = System.getProperty("loop.db.path", "database/loop.db");

		Class.forName("org.sqlite.JDBC");
		Connection connection=DriverManager.getConnection("jdbc:sqlite:"+path);
		
		String sql="SELECT * FROM product_"+tb;
		
		Statement statement=connection.createStatement();
		ResultSet rSet=statement.executeQuery(sql);
		
		
		ArrayList<ArrayList<String>> dataStorage=new ArrayList<>();
		
		switch (tb) {
			case("Products"): {
				while (rSet.next()) {
					
					ArrayList<String> col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("productID")));
					col.add(rSet.getString("productName"));
					col.add(rSet.getString("shortDescription"));
					col.add(rSet.getString("extendedDescription"));
					col.add(Double.toString(rSet.getDouble("cost")));
					col.add(Double.toString(rSet.getDouble("price")));
					col.add((rSet.getInt("status")==1)? "Active":"Inactive");
					col.add(Integer.toString(rSet.getInt("spiceLevel")));
					col.add(rSet.getString("country"));
					col.add(rSet.getString("createdDate"));
					col.add(rSet.getString("updatedDate"));
					col.add(Integer.toString(rSet.getInt("chefID")));
				
					dataStorage.add(col);
				}
				break;
			}
			case("ProductImage"): {
				while (rSet.next()) {
					
					ArrayList<String> col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("imageID")));
					col.add(rSet.getString("imageURL"));
					col.add(rSet.getString("altText"));
					col.add(Integer.toString(rSet.getInt("displayOrder")));
					col.add(rSet.getString("uploadDate"));
					col.add(Integer.toString(rSet.getInt("productID")));
				
					dataStorage.add(col);
				}
				break;
			}
			case("Ingredient"): {
				while (rSet.next()) {
					
					ArrayList<String> col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("ingredientID")));
					col.add(rSet.getString("ingredientName"));
					col.add(Double.toString(rSet.getDouble("calories")));
					col.add(Double.toString(rSet.getDouble("protein")));
					col.add(Double.toString(rSet.getDouble("carbohydrates")));
					col.add(Double.toString(rSet.getDouble("sugars")));
					col.add(Double.toString(rSet.getDouble("fat")));
					col.add(Double.toString(rSet.getDouble("saturatedFat")));
					col.add(Double.toString(rSet.getDouble("fiber")));
					col.add(Double.toString(rSet.getDouble("sodium")));
				
					dataStorage.add(col);
				}
				break;
			}
			case("DefaultIngredient"): {
				while (rSet.next()) {
					
					ArrayList<String> col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("defaultIngredientID")));
					col.add(rSet.getString("defaultIngredients"));
					col.add(Double.toString(rSet.getDouble("totalCalories")));
					col.add(Double.toString(rSet.getDouble("totalProtein")));
					col.add(Double.toString(rSet.getDouble("totalCarbohydrates")));
					col.add(Double.toString(rSet.getDouble("totalSugars")));
					col.add(Double.toString(rSet.getDouble("totalFat")));
					col.add(Double.toString(rSet.getDouble("totalSaturatedFat")));
					col.add(Double.toString(rSet.getDouble("totalFiber")));
					col.add(Double.toString(rSet.getDouble("totalSodium")));
					col.add(Integer.toString(rSet.getInt("productID")));
				
					dataStorage.add(col);
				}
				break;
			}
			case("Category"): {
				while (rSet.next()) {
					
					ArrayList<String> col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("categoryID")));
					col.add(rSet.getString("chosenSortBy")==null? "":rSet.getString("chosenSortBy"));
					col.add(rSet.getString("chosenDietary"));
					col.add(rSet.getString("chosenHealthGoal"));
					col.add(rSet.getString("chosenCuisines"));
					col.add(Integer.toString(rSet.getInt("productID")));
					
				
					dataStorage.add(col);
				}
				break;
			}
			case("Ratings"): {
				while (rSet.next()) {
					
					ArrayList<String>col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("rateID")));
					col.add(Double.toString(rSet.getDouble("rating")));
					col.add(Integer.toString(rSet.getInt("noPeople")));
					col.add(Integer.toString(rSet.getInt("productID")));
					
					dataStorage.add(col);
				}
				break;
			}
			case("Chef"): {
				while (rSet.next()) {
					ArrayList<String>col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("chefID")));
					col.add(rSet.getString("chefName"));
					col.add(Integer.toString(rSet.getInt("chefRating&ReviewID")));
					col.add(rSet.getString("chefDescription"));
					col.add(rSet.getString("chefTag1"));
					col.add(rSet.getString("chefTag2"));
					col.add(rSet.getString("chefTag3"));
					col.add(rSet.getString("chefImage"));
					col.add(rSet.getString("chefEmail"));
					col.add(rSet.getString("chefTel"));
					
					dataStorage.add(col);
				}
				break;
			}
			case("ChefReview"): {
				while (rSet.next()) {
					ArrayList<String>col=new ArrayList<>();
					
					col.add(Integer.toString(rSet.getInt("reviewID")));
					col.add(Integer.toString(rSet.getInt("chefID")));
					col.add(rSet.getString("reviewerName"));
					col.add(Double.toString(rSet.getDouble("rating")));
					col.add(rSet.getString("reviewText"));
					col.add(rSet.getString("reviewDate"));
					
					dataStorage.add(col);
				}
				break;
			}
		}
	
		connection.close();
		return dataStorage;
	}
	
	public static void addData(String tb, String[] columns, String[] dataTypes, ArrayList<String> values) throws ClassNotFoundException, SQLException {
		String path = System.getProperty("loop.db.path", "database/loop.db");
		
		Class.forName("org.sqlite.JDBC");		
		Connection connection=DriverManager.getConnection("jdbc:sqlite:"+path);
		
		String sql = "INSERT INTO product_"+tb+" (";
		if (columns.length==values.size()) {
			for (int i=0; i<columns.length; i++) {
				sql+=columns[i];
				
				if (i!=columns.length-1) {
					sql+=", ";
				}
			}
			
			sql+=") VALUES(";
			
			for (int j=0; j<columns.length; j++) {
				sql+="?";
				
				if (j!=columns.length-1) {
					sql+=", ";
				}
			}
			
			sql+=")";
		} else {
			System.out.println("Mismatch columns and values array size");
		}
		
		PreparedStatement pstmt=connection.prepareStatement(sql);
		
		for (int k=1; k<=values.size(); k++) {
			if (dataTypes[k-1].equalsIgnoreCase("String")) {
				pstmt.setString(k, values.get(k-1));
			} else if (dataTypes[k-1].equalsIgnoreCase("int")) {
				try {
				pstmt.setInt(k, Integer.parseInt(values.get(k-1)));
				} catch (NumberFormatException e) {
					System.out.println("Number Formatting failed by value at index "+k);
				}
			} else if (dataTypes[k-1].equalsIgnoreCase("double")) {
				try {
				pstmt.setDouble(k, Double.parseDouble(values.get(k-1)));
				} catch (NumberFormatException e) {
					System.out.println("Number Formatting failed by value at index "+k);
				}
			} else {
					System.out.println("Datatype not found on index "+k);
			}
		}
		
		 pstmt.executeUpdate();
		
		connection.close();
	}
	
	public static void deleteRow(String tb, String columnName, Object value) throws ClassNotFoundException, SQLException {
		String path = System.getProperty("loop.db.path", "database/loop.db");	
		
		Class.forName("org.sqlite.JDBC");
		Connection connection=DriverManager.getConnection("jdbc:sqlite:"+path);

	    String sql = "DELETE FROM product_"+tb+" WHERE "+columnName+" = ?";

	    PreparedStatement pstmt = connection.prepareStatement(sql);

	    if (value instanceof Integer) {
	        pstmt.setInt(1, (Integer) value);
	    } else if (value instanceof Double) {
	        pstmt.setDouble(1, (Double) value);
	    } else {
	        pstmt.setString(1, value.toString());
	    }

	    pstmt.executeUpdate();

	    connection.close();
	}
}
