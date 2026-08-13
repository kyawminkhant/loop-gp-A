package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import dao.FoodItemDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.InventoryDeliveryService;
import services.InventoryImageService;

/** Views and updates one ingredient at a selected warehouse. */
public final class FoodItemDetailPopupController {

    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label stockLabel;
    @FXML private Label messageLabel;
    @FXML private ImageView foodImageView;
    @FXML private ComboBox<String> warehouseComboBox;
    @FXML private TextField stockInput;

    private int ingredientId;

    @FXML
    private void initialize() {
        try {
            warehouseComboBox.getItems().setAll(
                    InventoryDeliveryService.loadWarehouseChoices());
            warehouseComboBox.getSelectionModel().selectFirst();
        } catch (Exception exception) {
            messageLabel.setText("Could not load warehouses.");
        }
        warehouseComboBox.setOnAction(event -> refreshStock());
    }

    public void setFoodData(
            String id, String name, String category, int stock, String imagePath) {
        try {
            ingredientId = Integer.parseInt(id);
        } catch (NumberFormatException exception) {
            ingredientId = -1;
        }
        idLabel.setText("ID: " + id);
        nameLabel.setText(name);
        categoryLabel.setText("Category: " + category);
        stockLabel.setText("All warehouses: " + stock + " units");
        refreshStock();

        InventoryImageService.loadImage(imagePath)
                .ifPresentOrElse(foodImageView::setImage, this::setDefaultImage);
    }

    @FXML
    private void handleUpdateStock() {
        String input = stockInput.getText();
        try {
            int quantity = Integer.parseInt(input == null ? "" : input.trim());
            updateStock(quantity);
        } catch (NumberFormatException exception) {
            messageLabel.setText("Enter a whole stock quantity.");
        }
    }

    @FXML
    private void handleSetZeroStock() {
        updateStock(0);
    }

    private void updateStock(int quantity) {
        String warehouse = warehouseComboBox.getValue();
        if (ingredientId <= 0 || warehouse == null) {
            messageLabel.setText("Select an ingredient and warehouse first.");
            return;
        }
        try {
            FoodItemDAO.updateStockForIngredientAtWarehouse(
                    ingredientId, warehouse, quantity);
            stockInput.clear();
            messageLabel.setText("Stock updated for " + warehouse + ".");
            refreshStock();
        } catch (Exception exception) {
            messageLabel.setText(exception.getMessage());
        }
    }

    private void refreshStock() {
        if (ingredientId <= 0 || warehouseComboBox.getValue() == null) {
            return;
        }
        try {
            String warehouse = warehouseComboBox.getValue();
            int quantity = FoodItemDAO.getStockForIngredientAtWarehouse(
                    ingredientId, warehouse);
            int capacity = FoodItemDAO.getCapacityForIngredientAtWarehouse(
                    ingredientId, warehouse);
            stockLabel.setText(warehouse + ": " + quantity + " / " + capacity + " units");
        } catch (Exception exception) {
            messageLabel.setText("Could not read warehouse stock.");
        }
    }

    private void setDefaultImage() {
        try {
            foodImageView.setImage(new Image(
                    getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception exception) {
            System.err.println("Default popup image could not load.");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) foodImageView.getScene().getWindow();
        stage.close();
    }
}
