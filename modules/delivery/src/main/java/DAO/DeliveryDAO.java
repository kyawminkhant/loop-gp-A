package DAO;

import model.Delivery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import Database.DBconnection;

public class DeliveryDAO {

    public static ObservableList<Delivery> getDelivery() {
        ObservableList<Delivery> list = FXCollections.observableArrayList();

        String sql = "SELECT * FROM delivery_AllDeliveries";

        try (Connection conn = DBconnection.getConnection();
        	     Statement stmt = conn.createStatement();
        	     ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Delivery(
                        rs.getString("DeliveryId"),
                        rs.getInt("OderId"),
                        rs.getString("Customer"),
                        rs.getInt("CusId"),
                        rs.getDouble("Total"),
                        rs.getString("Status"),
                        rs.getString("Driver"),
                        rs.getString("Action")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}