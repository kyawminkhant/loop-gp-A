package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Product_dashboard {

    @FXML
    private ImageView logo;

    @FXML
    private Button Home;

    @FXML
    private Button profileBtn;


    @FXML
    public void initialize(){

        logo.setImage(
            new Image(
                getClass()
                .getResourceAsStream("/images/logo.png")
            )
        );

    }


    @FXML
    private void scan() throws IOException {

        System.out.println("scan button clicked");

    }


    @FXML
    private void openAddStock(){

        try{

            FXMLLoader loader =
                    new FXMLLoader(
                        getClass()
                        .getResource("/fxmlFiles/add_stock.fxml")
                    );


            Parent root =
                    loader.load();


            Stage stage =
                    new Stage();


            stage.initModality(
                    Modality.APPLICATION_MODAL
            );


            stage.setTitle(
                    "Add Stock"
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
