package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class AddStockController {


    @FXML
    private ImageView foodImage;


    @FXML
    private TextField ingredientID;


    @FXML
    private TextField ingredientName;


    @FXML
    private TextField stockQuantity;


    @FXML
    private TextField warehouseID;


    @FXML
    private TextField capacity;



    private File selectedImage;



    @FXML
private void uploadImage(){

    FileChooser chooser = new FileChooser();

    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter(
            "Images",
            "*.png",
            "*.jpg",
            "*.jpeg"
        )
    );


    File file = chooser.showOpenDialog(
        foodImage.getScene().getWindow()
    );


    if(file != null){
        selectedImage = file;
        foodImage.setImage(new Image(file.toURI().toString()));
    }

}




    @FXML
    private void showConfirmation(){


        if(
            ingredientID.getText().isEmpty() ||
            ingredientName.getText().isEmpty() ||
            stockQuantity.getText().isEmpty() ||
            warehouseID.getText().isEmpty() ||
            capacity.getText().isEmpty()
        ){

            Alert alert =
                new Alert(
                    Alert.AlertType.WARNING
                );

            alert.setContentText(
                "Please fill all fields"
            );

            alert.showAndWait();

            return;

        }



        try{

            int quantity = Integer.parseInt(stockQuantity.getText().trim());
            int maximum = Integer.parseInt(capacity.getText().trim());
            if (quantity < 0 || maximum <= 0 || quantity > maximum) {
                showWarning("Stock quantity must be between 0 and the capacity.");
                return;
            }


            FXMLLoader loader =
                new FXMLLoader(
                    getClass()
                    .getResource(
                    "/fxmlfiles/ConfirmAddStock.fxml"
                    )
                );


            Parent root =
                    loader.load();



            ConfirmAddStockController controller =
                    loader.getController();



            controller.setData(

                ingredientID.getText(),

                ingredientName.getText(),

                quantity,

                warehouseID.getText(),

                maximum,

                selectedImage
            );



            Stage stage =
                    new Stage();


            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.initOwner(foodImage.getScene().getWindow());


            stage.setScene(
                    new Scene(root)
            );


            stage.showAndWait();



        }catch(NumberFormatException e){
            showWarning("Stock quantity and capacity must be whole numbers.");
        }catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could Not Add Ingredient");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();

        }

    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
   

}
