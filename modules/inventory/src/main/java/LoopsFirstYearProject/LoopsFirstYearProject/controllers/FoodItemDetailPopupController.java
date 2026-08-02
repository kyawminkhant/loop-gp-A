package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FoodItemDetailPopupController {

    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label stockLabel;
    @FXML private ImageView foodImageView;

    public void setFoodData(String id, String name, String category, int stock, String imagePath) {
        idLabel.setText("ID: " + id);
        nameLabel.setText(name);
        categoryLabel.setText("Category: " + category);
        stockLabel.setText("Stock Status: " + stock + " remaining");

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                foodImageView.setImage(new Image(imagePath));
            } catch (Exception e) {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            foodImageView.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.err.println("Default popup image could not load.");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) foodImageView.getScene().getWindow();
        stage.close();
    }
}