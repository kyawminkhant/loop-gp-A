package LoopProject.loopproject.controllers;

import DAO.DeliveryDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.App;
import model.Delivery;

import java.io.IOException;

public class AssigningDrivers {

    @FXML private VBox assignmentList;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        refreshAssignments();
    }

    private void refreshAssignments() {
        assignmentList.getChildren().clear();
        if (DeliveryDAO.getUnassignedDeliveries().isEmpty()) {
            Label empty = new Label("No orders are waiting for a driver.");
            empty.getStyleClass().add("customername");
            assignmentList.getChildren().add(empty);
            return;
        }

        for (Delivery delivery : DeliveryDAO.getUnassignedDeliveries()) {
            VBox card = new VBox(8);
            card.getStyleClass().add("Driver");

            Label order = new Label("Order #" + delivery.getOrderId()
                    + "  |  " + delivery.getDeliveryId());
            order.getStyleClass().add("order-id");
            Label customer = new Label(delivery.getCustomer()
                    + "  -  £" + String.format("%.2f", delivery.getTotal()));
            customer.getStyleClass().add("customername");

            ComboBox<String> driverChoice = new ComboBox<>(FXCollections.observableArrayList(
                    "Iman", "Efrin", "Prakash", "Jonny", "Samira"));
            driverChoice.setPromptText("Choose driver");
            Button assign = new Button("Assign");
            assign.getStyleClass().add("action-button");
            assign.setOnAction(event -> {
                String driver = driverChoice.getValue();
                if (driver == null) {
                    statusLabel.setText("Choose a driver for order #" + delivery.getOrderId() + ".");
                    return;
                }
                boolean updated = DeliveryDAO.assignDriver(delivery.getOrderId(), driver);
                statusLabel.setText(updated
                        ? "Order #" + delivery.getOrderId() + " assigned to " + driver + "."
                        : "Could not assign order #" + delivery.getOrderId() + ".");
                refreshAssignments();
            });

            card.getChildren().addAll(order, customer, new HBox(10, driverChoice, assign));
            assignmentList.getChildren().add(card);
        }
    }

    @FXML
    private void goBack() {
        setRoot("delivery");
    }

    @FXML
    private void showActiveDeliveries() {
        setRoot("activeDeliveries");
    }

    private void setRoot(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not open " + fxml + ".");
        }
    }
}
