package LoopsFirstYearProject.LoopsFirstYearProject.controllers;


import dao.IngredientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;
import services.InventoryImageService;

import java.io.File;


public class ConfirmAddStockController {


    @FXML
    private ImageView image;


    @FXML
    private Label details;



    private String id;
    private String name;
    private int quantity;
    private String warehouse;
    private int capacity;
    private File selectedImage;



    public void setData(
            String id,
            String name,
            int quantity,
            String warehouse,
            int capacity,
            File selectedImage
    ){

        this.id=id;
        this.name=name;
        this.quantity=quantity;
        this.warehouse=warehouse;
        this.capacity=capacity;
        this.selectedImage=selectedImage;


        details.setText(
            "ID: "+id+
            "\nName: "+name+
            "\nStock: "+quantity+
            "\nWarehouse: "+warehouse+
            "\nCapacity: "+capacity
        );


        if(selectedImage != null){
            image.setImage(new Image(selectedImage.toURI().toString()));
        }

    }




    @FXML
    private void confirm(){


        boolean saved;
        try {
            String imagePath = InventoryImageService.storeUploadedImage(selectedImage);
            saved = IngredientDAO.addIngredient(
                    id,
                    name,
                    quantity,
                    warehouse,
                    capacity,
                    imagePath
            );
        } catch (Exception exception) {
            saved = false;
            exception.printStackTrace();
        }

        if (saved) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Stock Added");
            alert.setHeaderText(null);
            alert.setContentText("The ingredient and its stock were saved to the shared database.");
            alert.showAndWait();
            closeAll();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could Not Add Stock");
            alert.setHeaderText("Inventory was not changed");
            alert.setContentText("Check that the stock code is unique and quantity does not exceed capacity.");
            alert.showAndWait();
        }

    }



    @FXML
    private void cancel(){

        close();

    }



    private void close(){

        Stage stage =
                (Stage) details
                .getScene()
                .getWindow();

        stage.close();

    }

    private void closeAll() {
        Stage confirmation = (Stage) details.getScene().getWindow();
        Window addIngredientWindow = confirmation.getOwner();
        confirmation.close();
        if (addIngredientWindow instanceof Stage) {
            ((Stage) addIngredientWindow).close();
        }
    }

}
