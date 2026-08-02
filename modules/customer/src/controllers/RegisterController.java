package controllers;

import database.CustomerDAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.AnimationUtil;
import utils.ImageFileUtil;
import utils.NavigationUtil;
import utils.ValidationUtil;

import java.io.File;

public class RegisterController {

    @FXML private ImageView logoImage;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField mobileField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField addressField;
    @FXML private TextField idCardField;
    @FXML private ImageView idCardPreview;
    @FXML private Label idCardFileLabel;
    @FXML private Label messageLabel;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private String selectedIdCardImagePath;

    @FXML
    public void initialize() {
        mobileField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("[0-9]{0,15}") ? change : null;
        }));

        idCardField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("[a-zA-Z0-9 -]{0,30}") ? change : null;
        }));

        Platform.runLater(() -> {
            Parent root = nameField.getScene() != null ? nameField.getScene().getRoot() : null;
            if (root != null) {
                Node card = root.lookup(".glass-card");
                if (card != null) {
                    AnimationUtil.popIn(card);
                }
                AnimationUtil.enableButtonEffects(root);
            }
        });
    }

    @FXML
    private void handleChooseIdCardImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select ID Card Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        try {
            selectedIdCardImagePath = ImageFileUtil.saveIdCardImage(file);
            Image preview = ImageFileUtil.loadImage(selectedIdCardImagePath);
            idCardPreview.setImage(preview);
            idCardFileLabel.setText(file.getName());
        } catch (Exception e) {
            showError("Could not load ID card image. Please choose a valid image file.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String mobile = mobileField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String address = addressField.getText().trim();
        String idCard = idCardField.getText().trim();

        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isNotEmpty(email)
                || !ValidationUtil.isNotEmpty(mobile) || !ValidationUtil.isNotEmpty(password)
                || !ValidationUtil.isNotEmpty(address) || !ValidationUtil.isNotEmpty(idCard)) {
            showError("All fields are required.");
            return;
        }

        if (selectedIdCardImagePath == null || selectedIdCardImagePath.isBlank()) {
            showError("Please upload a picture of your ID card.");
            return;
        }

        if (ValidationUtil.containsUnsafeCharacters(name) || ValidationUtil.containsUnsafeCharacters(address)) {
            showError("Name and address cannot contain special characters like @#/\\?\"'~`$");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (!ValidationUtil.isValidMobile(mobile)) {
            showError("Mobile number must be 10-15 digits.");
            return;
        }

        if (!ValidationUtil.isValidIdCard(idCard)) {
            showError("ID card must be 4-30 characters using letters, numbers, dash or space.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (customerDAO.emailExists(email)) {
            showError("Email already registered.");
            return;
        }

        if (customerDAO.idCardExists(idCard)) {
            showError("ID card already in use.");
            return;
        }

        boolean success = customerDAO.registerCustomer(
                name, email, mobile, password, address, idCard, selectedIdCardImagePath
        );

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Account created successfully. You can now log in.");
            alert.showAndWait();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        AnimationUtil.shake(messageLabel);
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
    }
}
