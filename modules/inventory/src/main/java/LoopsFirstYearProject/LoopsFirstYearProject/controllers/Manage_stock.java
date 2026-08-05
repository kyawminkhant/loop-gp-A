package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
                    .getResource("/fxmlFiles/transferItems.fxml")
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

            for (TransferItems item : transferItems) {
                if (item.getFromAddress() == null || item.getToAddress() == null
                        || item.getProduct() == null || item.getQuantity() <= 0) {
                    showWarning("Select both warehouses and an ingredient, then enter a quantity above zero.");
                    return;
                }
                if (item.getFromAddress().equalsIgnoreCase(item.getToAddress())) {
                    showWarning("The source and destination warehouses must be different.");
                    return;
                }
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

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Stock Transfer");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void backToDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (Exception exception) {
            throw new IllegalStateException("Could not return to Inventory Dashboard.", exception);
        }
    }

}
