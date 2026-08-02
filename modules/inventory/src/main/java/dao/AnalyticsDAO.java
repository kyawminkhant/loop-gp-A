package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Analytics;

public class AnalyticsDAO {

	public static ObservableList<Analytics> getLocations(){
		ObservableList<Analytics> list = FXCollections.observableArrayList();
		
		String sql = "SELECT * FROM inventory_stock_analytics";
		
		try(Connection conn = DBConnection.getConnectionURLlocation();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			while(rs.next()) {
				list.add(new Analytics(
						rs.getString("product"),
						rs.getString("locations"),
						rs.getInt("currentQuantity"),
						rs.getInt("minThreshold"),
						rs.getString("status"),
						rs.getString("lastRestock"),
						rs.getInt("daysUntilReorder")
						));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	
}