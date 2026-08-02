package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.sql.ResultSet;


public class Signup {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabelAccess;

    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        logoImage.setImage(
            new Image(getClass().getResourceAsStream("/images/logo.png"))
        );
    }

    @FXML
    private void handleLogin() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
        	messageLabelAccess.setText("Fill all fields!");
            return;
        }

        String sql = "INSERT INTO inventory_stock_UserAccounts (Username, Password) VALUES (?, ?)";
        String checkSQL =" SELECT 1 FROM inventory_stock_UserAccounts Where Username = ? ";

        try (Connection conn = DBConnection.getConnectionURLlocation()){
        	System.out.println("Mongo DB connected");
        	try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)){
        	checkStmt.setString(1, username);
        	ResultSet rs = checkStmt.executeQuery();
        	
        	
        	if(rs.next()) {messageLabelAccess.setText("UserName Already Exists!");
        	return ;
        	} 
        	}
        
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                messageLabelAccess.setText ("Signup successful!");
                App.setRoot("login");
            } else {
                messageLabelAccess.setText("Signup failed!");
            }
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabelAccess.setText("Error: " + e.getMessage());
        }
    }
}
