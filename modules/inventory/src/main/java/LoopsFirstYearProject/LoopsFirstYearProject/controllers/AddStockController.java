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



    private String imagePath;



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

        try{

            File destination =
            new File(
            "src/main/resources/FoodImage/"
            +file.getName()
            );


            java.nio.file.Files.copy(
                file.toPath(),
                destination.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );


            imagePath =
            "/FoodImage/" + file.getName();


            foodImage.setImage(
                new Image(
                    destination.toURI().toString()
                )
            );


        }catch(Exception e){

            e.printStackTrace();

        }

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

                Integer.parseInt(
                    stockQuantity.getText()
                ),

                warehouseID.getText(),

                Integer.parseInt(
                    capacity.getText()
                ),

                imagePath
            );



            Stage stage =
                    new Stage();


            stage.initModality(
                    Modality.APPLICATION_MODAL
            );


            stage.setScene(
                    new Scene(root)
            );


            stage.showAndWait();



        }catch(Exception e){

            e.printStackTrace();

        }

    }
   

}