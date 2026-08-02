package gp.loop;

import gp.loop.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Sign-up / login screen fronting the finance component (design: FinanceAuthService). */
public class SignupController {

    private final AuthService auth = new AuthService();

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleSignup() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        if (!email.contains("@")) {
            showError("Please enter a valid email address");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match");
            return;
        }

        try {
            if (!auth.register(email, password)) {
                showError("An account with this email already exists");
                return;
            }
            App.setRoot("finance-dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Sign up failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        try {
            if (!auth.authenticate(email, password)) {
                showError("Incorrect email or password");
                return;
            }
            App.setRoot("finance-dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
