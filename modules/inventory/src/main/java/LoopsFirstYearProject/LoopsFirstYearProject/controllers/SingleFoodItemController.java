package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.FoodItem;
import services.InventoryImageService;

public class SingleFoodItemController {

    @FXML
    private Label ingredientId;   
    
    @FXML
    private Label ingredientName; 
    
    @FXML
    private ImageView foodImage;

    public void setFoodData(FoodItem item) {

        ingredientId.setText("ID: " + item.getID());
        ingredientName.setText(item.getName());

        String path = item.getURLPath();

        InventoryImageService.loadImage(path).ifPresentOrElse(
                foodImage::setImage,
                () -> foodImage.setImage(new Image(
                        getClass().getResourceAsStream("/images/logo.png"))));
    }
}
