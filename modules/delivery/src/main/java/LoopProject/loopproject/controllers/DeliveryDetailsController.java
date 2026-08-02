package LoopProject.loopproject.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Database.DBconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import main.App;
import model.DeliveryDetails;

public class DeliveryDetailsController {

    @FXML
    private TableView<DeliveryDetails> deliverydetailsTable;

    @FXML
    private TableColumn<DeliveryDetails, String> deliveryIdColumn;

    @FXML
    private TableColumn<DeliveryDetails, String> customerNameColumn;

    @FXML
    private TableColumn<DeliveryDetails, String> customerAddressColumn;

    @FXML
    public void initialize() {

        deliverydetailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        deliveryIdColumn.setCellValueFactory(new PropertyValueFactory<>("DeliveryId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("CustomerAddress"));
        
        deliverydetailsTable.setItems(loadDeliveryDetails());
    }
    
    @FXML
    private void goBack() {
        try {
            App.setRoot("delivery");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


        private static ObservableList<DeliveryDetails> loadDeliveryDetails() {
            ObservableList<DeliveryDetails> deliveries = FXCollections.observableArrayList();

            String sql = "SELECT DeliveryId, CustomerName, CustomerAddress FROM delivery_DeliveryDetails";

            try (Connection conn = DBconnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    deliveries.add(new DeliveryDetails(
                            rs.getString("DeliveryId"),
                            rs.getString("CustomerName"),
                            rs.getString("CustomerAddress")
                    ));
                }

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText("Could not load delivery details: " + e.getMessage());
                alert.showAndWait();
            }

            return deliveries;

    
    }
}