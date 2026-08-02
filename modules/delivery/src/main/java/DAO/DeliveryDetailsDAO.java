package DAO;

import model.DeliveryDetails;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

import Database.DBconnection;



public class DeliveryDetailsDAO {


	
	public static ObservableList<DeliveryDetails> getDelivery(){
	ObservableList<DeliveryDetails> list = FXCollections.observableArrayList();

	String sql = "SELECT * FROM delivery_DeliveryDetails " ;

	try(Connection conn = DBconnection.getConnection();
	Statement stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery(sql)) {
	while (rs.next()) {

	list.add(new DeliveryDetails(
	rs.getString("DeliveryId"),
	rs.getString("CustomerName"),
	rs.getString("CustomerAddress")
	));
	}

	} catch (Exception e) {
	e.printStackTrace();
	}

	return list;
	}

}
