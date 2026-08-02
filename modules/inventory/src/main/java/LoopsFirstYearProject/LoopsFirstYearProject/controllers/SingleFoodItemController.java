package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.FoodItem;

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

        System.out.println("DB Path: " + path);
        System.out.println("URL: " + getClass().getResource(path));

        Image image = new Image(
                getClass().getResourceAsStream(path)
        );

        foodImage.setImage(image);
    }
}