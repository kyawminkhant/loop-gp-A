package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import dao.FoodItemDAO;
import dao.LocationsDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TransferItemsController {

    private int stockQuantity = 0;
    private String message = "The quantity must be above zero";
    
    @FXML
    private Label warningMessage;
    
    @FXML
    private ComboBox<String> fromAddress;
    
    @FXML
    private ComboBox<String> toAddress;
    
    @FXML
    private ComboBox<String> product;
 
    @FXML
    private TextField reason;
    
    @FXML
    private Label stockCount;
    
    private String fromAddress1;
    private String toAddress1;
    private String product1;
    private String reason1;
    
    public void setData(String fromAddress1, String toAddress1, 
            String product1, int quantity1, String reason1) {

        this.fromAddress1 = fromAddress1;
        this.toAddress1 = toAddress1;
        this.product1 = product1;
        this.reason1 = reason1;
        this.stockQuantity = quantity1;

        // CRITICAL FIX: Push values directly to JavaFX elements so they appear in UI
        if (fromAddress != null) fromAddress.setValue(fromAddress1);
        if (toAddress != null) toAddress.setValue(toAddress1);
        if (product != null) product.setValue(product1);
        if (reason != null) reason.setText(reason1);
        showCount();
    }
    
    @FXML
    public void initialize() {
        fromAddress.setItems(LocationsDAO.getLocations("moreStorageLocations"));
        toAddress.setItems(LocationsDAO.getLocations("moreStorageLocations"));        
        
        // FIX: Populating with String list instead of FoodItem list
        product.setItems(FoodItemDAO.getAllFoodIngredientNames());
    }
    
    // GETTERS (Cleaned of compile-disruptive non-breaking spaces)
    public String getFromAddress() {
        return fromAddress.getValue();
    }

    public String getToAddress() {
        return toAddress.getValue();
    }

    public String getProduct() {
        return product.getValue();
    }

    public String getReason() {
        return reason.getText();
    }

    public int getQuantity() {
        return stockQuantity;
    }
    
    public void handleIncrement(ActionEvent e) {
        stockQuantity++;
        showCount();
        warningMessage.setText("");
    }
    
    public void handleDelete(ActionEvent e) {
        stockQuantity = 0;
        showCount();
        warningMessage.setText("");
    }
    
    private void showCount() {
        stockCount.setText(String.valueOf(stockQuantity));
    }
    
    public void handleDecrement(ActionEvent e) {
        if (stockQuantity <= 0) {
            warningMessage.setText(message);
            showCount();
        } else {
            stockQuantity--;
            showCount();
        }
    }
}