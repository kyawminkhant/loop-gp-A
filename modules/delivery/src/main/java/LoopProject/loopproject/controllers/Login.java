package LoopProject.loopproject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.App;

import java.io.*;

public class Login {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private ComboBox<String>cmbUser;
    
    @FXML
    public void initialize() {
    	cmbUser.getItems().addAll("Admin", "Driver");
    }
 
    @FXML
    private void login() throws IOException
    {
    	
    	String username = txtUsername.getText();
    	String password = txtPassword.getText();
    	String SelectedUser = cmbUser.getValue();
    	
    	
    	if(username.isEmpty()|| password.isEmpty()) {
    		Alert alert = new Alert(Alert.AlertType.ERROR);
    		alert.setTitle("Retry To Login");
    		alert.setHeaderText(null);
    		alert.setContentText("Please Enter Username and Password again");
    		alert.showAndWait();
    		return;
    	}

    	if (SelectedUser==null) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Select User");
        alert.setHeaderText(null);
        alert.setContentText("Please Select Admin or Driver");
        alert.showAndWait();
        return;
    	}
    
    
   
    	
    	if ("Admin".equals(SelectedUser)) {
    	    App.setRoot("Delivery");
    	} else if ("Driver".equals(SelectedUser)) {
    	    App.setRoot("deliveryaccept");
    	}
    }
    
    
}