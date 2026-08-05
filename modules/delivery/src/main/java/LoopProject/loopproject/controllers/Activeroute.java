package LoopProject.loopproject.controllers;

import DAO.DeliveryDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import main.App;
import model.Delivery;

import java.io.IOException;

public class Activeroute {

    @FXML private VBox routeContainer;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        refreshRoutes();
    }

    private void refreshRoutes() {
        routeContainer.getChildren().clear();
        if (DeliveryDAO.getActiveDeliveries().isEmpty()) {
            routeContainer.getChildren().add(new Label("No active routes."));
            return;
        }

        for (Delivery delivery : DeliveryDAO.getActiveDeliveries()) {
            VBox route = new VBox(7);
            route.getStyleClass().add("route");
            Label heading = new Label(delivery.getDeliveryId()
                    + "  |  Order #" + delivery.getOrderId());
            heading.getStyleClass().add("delivery-label");
            Label details = new Label(delivery.getDriver() + " → " + delivery.getCustomer());
            details.getStyleClass().add("route-text");
            Label state = new Label(delivery.getStatus());
            state.getStyleClass().add("status-button");
            Button arrived = new Button("Arrived at Location");
            arrived.getStyleClass().add("arrived-button");
            arrived.setOnAction(event -> markArrived(delivery));
            route.getChildren().addAll(heading, details, state, arrived);
            routeContainer.getChildren().add(route);
        }
    }

    private void markArrived(Delivery delivery) {
        if (!DeliveryDAO.markDelivered(delivery.getOrderId())) {
            statusLabel.setText("Could not complete order #" + delivery.getOrderId() + ".");
            return;
        }
        try {
            App.setRoot("deliveredsuccess");
        } catch (IOException exception) {
            exception.printStackTrace();
            statusLabel.setText("Order delivered, but the confirmation page could not open.");
        }
    }
}
