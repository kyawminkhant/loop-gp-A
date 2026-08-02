package LoopsFirstYearProject.LoopsFirstYearProject.controllers;


import dao.IngredientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


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
    private String imagePath;



    public void setData(
            String id,
            String name,
            int quantity,
            String warehouse,
            int capacity,
            String imagePath
    ){

        this.id=id;
        this.name=name;
        this.quantity=quantity;
        this.warehouse=warehouse;
        this.capacity=capacity;
        this.imagePath=imagePath;


        details.setText(
            "ID: "+id+
            "\nName: "+name+
            "\nStock: "+quantity+
            "\nWarehouse: "+warehouse+
            "\nCapacity: "+capacity
        );


        if(imagePath != null){

            var stream = getClass()
                    .getResourceAsStream(imagePath);

            if(stream != null){

                image.setImage(new Image(stream));

            } else {

                System.out.println("Image not found: " + imagePath);

            }
        }

    }




    @FXML
    private void confirm(){


        IngredientDAO.addIngredient(
                id,
                name,
                quantity,
                warehouse,
                capacity,
                imagePath
        );


        close();

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

}