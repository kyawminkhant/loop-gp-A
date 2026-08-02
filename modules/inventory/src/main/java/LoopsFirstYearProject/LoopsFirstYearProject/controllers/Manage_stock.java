package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.TransferItems;

import java.util.ArrayList;
import java.util.List;


public class Manage_stock {


    @FXML
    private ImageView logo;


    @FXML
    private VBox transferContainer;


    private List<TransferItemsController> transferControllers = new ArrayList<>();



    @FXML
    public void initialize() {

        logo.setImage(
            new Image(
                getClass()
                .getResourceAsStream("/images/logo.png")
            )
        );


        // add first transfer section automatically
        addTransferItem();

    }



    @FXML
    private void addTransferItem() {


        try {


            FXMLLoader loader =
                new FXMLLoader(
                getClass()
                .getResource("/fxmlfiles/transferItems.fxml")
                );


            Parent transferItem = loader.load();



            TransferItemsController controller =
                    loader.getController();



            transferControllers.add(controller);



            transferContainer
                    .getChildren()
                    .add(transferItem);



        } catch(Exception e) {

            e.printStackTrace();

        }

    }





    @FXML
    private void openSummary() {


        try {


            List<TransferItems> transferItems =
                    new ArrayList<>();



            for(TransferItemsController controller : transferControllers) {


                TransferItems item =
                        new TransferItems();


                item.setFromAddress(
                        controller.getFromAddress()
                );


                item.setToAddress(
                        controller.getToAddress()
                );


                item.setProduct(
                        controller.getProduct()
                );


                item.setQuantity(
                        controller.getQuantity()
                );


                item.setReason(
                        controller.getReason()
                );


                transferItems.add(item);

            }



            FXMLLoader loader =
                    new FXMLLoader(
                    getClass()
                    .getResource("/modalPopups/confirmManageStock.fxml")
                    );


            Parent root = loader.load();



            ManageStockPopup popup =
                    loader.getController();



            popup.loadSummary(
                    transferItems
            );



            Stage stage = new Stage();


            stage.initModality(
                    Modality.APPLICATION_MODAL
            );


            stage.setScene(
                    new Scene(root)
            );


            stage.showAndWait();



        } catch(Exception e) {

            e.printStackTrace();

        }


    }

}