package LoopProject.loopproject.controllers;

import java.io.IOException;

import DAO.DeliveryDAO;
import main.App;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class Delivery {

    @FXML private TableView<model.Delivery> deliveryTable;
    @FXML private TableColumn<model.Delivery, String> deliveryId;
    @FXML private TableColumn<model.Delivery, Integer> orderId;
    @FXML private TableColumn<model.Delivery, String> customer;
    @FXML private TableColumn<model.Delivery, Integer> customerId;
    @FXML private TableColumn<model.Delivery, Double> total;
    @FXML private TableColumn<model.Delivery, String> status;
    @FXML private TableColumn<model.Delivery, String> driver;
    @FXML private TableColumn<model.Delivery, String> action;

    @FXML
    public void initialize() {
        deliveryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        deliveryId.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        orderId.setCellValueFactory(new PropertyValueFactory<>("oderId"));
        customer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        customerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        driver.setCellValueFactory(new PropertyValueFactory<>("driver"));
        action.setCellValueFactory(new PropertyValueFactory<>("action"));
        
        
        deliveryTable.setItems(DeliveryDAO.getDelivery());
    deliveryTable.setOnMouseClicked(event -> {
        model.Delivery selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                App.setRoot("deliverydetails");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}
    
    @FXML
    private void handleAssignDrivers() throws IOException {
        App.setRoot("assigningdrivers");
    }
    
    @FXML
    private void showSummary() throws IOException {
        App.setRoot("deliverysummary");
    }
    
    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }
}