package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import dao.StockTransferDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.TransferItems;

import java.util.List;


public class ManageStockPopup {


    @FXML
    private VBox summaryContainer;

    private List<TransferItems> transferList;


    /*
     * This method will receive all transfer items
     * from ManageStock
     */
    public void loadSummary(List<TransferItems> transferItems) {

        this.transferList = transferItems;

        summaryContainer.getChildren().clear();

        for (TransferItems item : transferItems) {

            createSummaryBox(item);

        }
    }
    
    private void createSummaryBox(TransferItems item) {

        VBox box = new VBox(12);
        box.getStyleClass().add("summary-card");


        Label label = new Label(
                "From: " + item.getFromAddress()
              + "\nTo: " + item.getToAddress()
              + "\nProduct: " + item.getProduct()
              + "\nQuantity: " + item.getQuantity()
              + "\nReason: " + item.getReason()
              + "\nManager Name: " + item.getManager()
        );

        label.getStyleClass().add("summary-text");


        Button removeButton = new Button("Remove");

        removeButton.getStyleClass().add("remove-btn");


        removeButton.setOnAction(e -> {

            transferList.remove(item);

            summaryContainer
                    .getChildren()
                    .remove(box);

        });


        box.getChildren()
           .addAll(label, removeButton);


        summaryContainer
                .getChildren()
                .add(box);

    }

    @FXML
    private void cancel() {


        Stage stage =
                (Stage) summaryContainer
                .getScene()
                .getWindow();


        stage.close();

    }




    @FXML
    private void confirmTransfer() {

        try {
            StockTransferDAO.transferAll(transferList);
        } catch (Exception exception) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Transfer Failed");
            error.setHeaderText("Inventory was not changed");
            error.setContentText(exception.getMessage());
            error.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Transfer Complete");
        alert.setHeaderText(null);
        alert.setContentText(
                "Stock transfer completed and saved to the shared database."
        );

        alert.showAndWait();



        Stage stage =
                (Stage) summaryContainer
                .getScene()
                .getWindow();


        stage.close();

    }

}
