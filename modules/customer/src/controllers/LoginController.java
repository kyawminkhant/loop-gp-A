package controllers;

import database.CustomerDAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Customer;
import utils.AnimationUtil;
import utils.NavigationUtil;
import utils.SessionManager;
import utils.ValidationUtil;

public class LoginController {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 30_000;

    private static int failedAttempts = 0;
    private static long lockUntilMs = 0;

    @FXML private ImageView logoImage;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Parent root = emailField.getScene() != null ? emailField.getScene().getRoot() : null;
            if (root != null) {
                AnimationUtil.enableButtonEffects(root);
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        long now = System.currentTimeMillis();
        if (now < lockUntilMs) {
            long secondsLeft = (lockUntilMs - now + 999) / 1000;
            messageLabel.setText("Too many failed attempts. Try again in " + secondsLeft + " seconds.");
            AnimationUtil.shake(messageLabel);
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(password)) {
            messageLabel.setText("Please enter both email and password.");
            AnimationUtil.shake(messageLabel);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            messageLabel.setText("Please enter a valid email address.");
            AnimationUtil.shake(messageLabel);
            return;
        }

        Customer customer = customerDAO.authenticate(email, password);

        if (customer == null) {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                lockUntilMs = System.currentTimeMillis() + LOCKOUT_MS;
                failedAttempts = 0;
                messageLabel.setText("Too many failed attempts. Please wait 30 seconds.");
            } else {
                messageLabel.setText("Invalid email or password, or account is inactive.");
            }
            AnimationUtil.shake(messageLabel);
            return;
        }

        failedAttempts = 0;
        lockUntilMs = 0;
        SessionManager.setCurrentCustomer(customer);

        if (SessionManager.openPersonalizedProducts()) {
            AnimationUtil.disableBackgroundAmbient();
            return;
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/Dashboard.fxml", "/styles/app.css");
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/Register.fxml", "/styles/app.css");
    }

    @FXML
    private void handleGoToSuperAdmin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/SuperAdmin.fxml", "/styles/app.css");
    }
}
