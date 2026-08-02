package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.UserDao;
import loop.reviews.model.User;
import loop.reviews.util.Validation;

/** FR1 - role-based login. */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (!Validation.isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }
        if (!Validation.isValidPassword(password)) {
            errorLabel.setText("Password must be at least 8 characters.");
            return;
        }

        User user = userDao.findByEmailAndPassword(email, password);
        if (user == null) {
            errorLabel.setText("Invalid credentials");   // FR1 failure message
            return;
        }
        Session.setCurrentUser(user);
        SceneManager.switchTo("home");
    }
}
