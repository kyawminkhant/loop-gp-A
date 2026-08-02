package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import Utils.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import javafx.fxml.FXML; 
import model.User;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.*;

public class Login {

	@FXML
	private TextField usernameField;
	
	@FXML
	private Label messageLabelAccess;

	@FXML
	private PasswordField passwordField;

	@FXML
	private ImageView logoImage;
	
	@FXML
	private void handleSignUp() {
		try {
			App.setRoot("signup");
			}catch(IOException e){
				e.printStackTrace();
			}
	}
	
	@FXML
	public void initialize() {
	    logoImage.setImage(
	        new Image(getClass().getResourceAsStream("/images/logo.png"))
	    );
	}

	@FXML
	private void handleLogin() throws IOException {
		String username = usernameField.getText().trim();
		String password = passwordField.getText();

		
	    String sql = "SELECT * FROM inventory_stock_UserAccounts WHERE Username=? AND Password=?";

	    try (Connection conn = DBConnection.getConnectionURLlocation();
	    	 PreparedStatement stmt = conn.prepareStatement(sql);){
	    	
	    	stmt.setString(1, username);
	    	stmt.setString(2, password);
	    	
	    	ResultSet rs = stmt.executeQuery();
	    	
	    
		if(rs.next()) {
			
			User user = new User(
		            rs.getString("Username"),
		            rs.getString("Password")
		    );
			
			messageLabelAccess.setText("Login Success!");
		    Session.setUser(user);
		    App.setRoot("dashboard");
		} else {
			messageLabelAccess.setText("Access Denied!");
		}
		
	    }catch(Exception e) {
	    	e.printStackTrace();
	    	
	    }
	}
}
