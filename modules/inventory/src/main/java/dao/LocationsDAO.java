package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.StorageLocations;

public class LocationsDAO {

	public static ObservableList<StorageLocations> getStorageLocations(String tablename){
		ObservableList<StorageLocations> list = FXCollections.observableArrayList();
		
		String sql = "SELECT * FROM inventory_stock_moreStorageLocations";
		
		try(Connection conn = DBConnection.getConnectionURLlocation();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			while(rs.next()) {
				list.add(new StorageLocations(
						rs.getString("name"),
						rs.getString("locations"),
						rs.getInt("locationID"),
						rs.getInt("capacity"),
						rs.getInt("currentStock"),
						rs.getString("manager")
						));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	
	
	public static ObservableList<String> getLocations(String tablename){
		ObservableList<String> list = FXCollections.observableArrayList();
		
		String sql = "SELECT locations FROM inventory_stock_moreStorageLocations";
		
		try(Connection conn = DBConnection.getConnectionURLlocation();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql)) {
			while(rs.next()) {
				list.add(rs.getString("locations"));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
}