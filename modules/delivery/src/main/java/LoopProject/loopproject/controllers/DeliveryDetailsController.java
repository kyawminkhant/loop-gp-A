package LoopProject.loopproject.controllers;

import DAO.DeliveryDetailsDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import main.App;
import model.DeliveryDetails;

import java.io.IOException;

public class DeliveryDetailsController {

    @FXML private TableView<DeliveryDetails> deliverydetailsTable;
    @FXML private TableColumn<DeliveryDetails, String> deliveryIdColumn;
    @FXML private TableColumn<DeliveryDetails, Integer> orderIdColumn;
    @FXML private TableColumn<DeliveryDetails, String> customerNameColumn;
    @FXML private TableColumn<DeliveryDetails, String> customerAddressColumn;
    @FXML private TableColumn<DeliveryDetails, String> statusColumn;
    @FXML private TableColumn<DeliveryDetails, String> driverColumn;

    @FXML
    public void initialize() {
        deliverydetailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        deliveryIdColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryId"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driver"));
        deliverydetailsTable.setItems(DeliveryDetailsDAO.getDelivery());
    }

    @FXML
    private void goBack() {
        try {
            App.setRoot("delivery");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
