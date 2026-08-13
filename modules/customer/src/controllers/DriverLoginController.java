package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.AnimationUtil;
import utils.DriverAuthService;
import utils.NavigationUtil;
import utils.SessionManager;
import utils.ValidationUtil;

public class DriverLoginController {

    @FXML private TextField driverIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Parent root = driverIdField.getScene() == null
                    ? null : driverIdField.getScene().getRoot();
            if (root != null) {
                AnimationUtil.enableButtonEffects(root);
            }
        });
    }

    @FXML
    private void handleLogin() {
        String driverId = driverIdField.getText();
        String password = passwordField.getText();

        if (!ValidationUtil.isNotEmpty(driverId) || !ValidationUtil.isNotEmpty(password)) {
            showError("Please enter both driver ID and password.");
            return;
        }

        String driverName = DriverAuthService.authenticate(driverId, password);
        if (driverName == null) {
            showError("Invalid driver ID or password.");
            return;
        }

        SessionManager.setCurrentDriverName(driverName);
        if (!SessionManager.openDriverDelivery()) {
            SessionManager.clearDriver();
            showError("Driver Delivery is available from the integrated Team Hub.");
        }
    }

    @FXML
    private void handleBackToCustomerLogin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        AnimationUtil.shake(messageLabel);
    }
}
