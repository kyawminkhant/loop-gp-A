package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import dao.IngredientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.FoodItem;

public class FoodItemDetailPopupController {

    @FXML
    private Label foodName;

    @FXML
    private Label foodID;

    @FXML
    private Label stock;

    @FXML
    private Label capacity;

    @FXML
    private TextField stockInput;

    @FXML
    private Button zeroStockButton;

    private FoodItem food;
    private IngredientDAO ingredientDAO = new IngredientDAO();

    @FXML
    public void initialize() {
        zeroStockButton.setOnAction(event -> handleSetZeroStock());
        stockInput.setOnAction(event -> handleUpdateStock());
    }

    public void setIngredientDetails(FoodItem selectedFood) {
        if (selectedFood == null) {
            foodName.setText("Name: Not Found");
            foodID.setText("ID: Not Found");
            stock.setText("Stock: Not Found");
            capacity.setText("Capacity: Not Found");
            return;
        }

        this.food = selectedFood;

        foodName.setText("Name: " + food.getName());
        foodID.setText("ID: " + food.getIngredientID());
        stock.setText("Stock: " + food.getStockQuantity());
        capacity.setText("Capacity: " + food.getCapacity());
    }

    @FXML
    private void handleUpdateStock() {
        if (food == null) return;

        String inputText = stockInput.getText();
        if (inputText == null || inputText.trim().isEmpty()) return;

        try {
            int newStock = Integer.parseInt(inputText.trim());
            updateStockValue(newStock);
            stockInput.clear();
        } catch (NumberFormatException e) {
            System.err.println("Invalid stock input: " + inputText);
        }
    }

    @FXML
    private void handleSetZeroStock() {
        if (food == null) return;
        updateStockValue(0);
    }

    private void updateStockValue(int newStockQuantity) {
        food.setStock(newStockQuantity);

        stock.setText("Stock: " + newStockQuantity);

        IngredientDAO.updateStock(food.getIngredientID(), newStockQuantity);
    }
}