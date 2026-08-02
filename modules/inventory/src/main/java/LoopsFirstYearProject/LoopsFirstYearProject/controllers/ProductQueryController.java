package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import model.FoodItem;

import services.ImageSaveService;
import services.ProductSearchService;
import services.QRScannerService;
import services.WebCamService;

import dao.FoodItemDAO;

import java.awt.image.BufferedImage;
import java.util.Optional;


public class ProductQueryController {


    @FXML
    private ImageView cameraView;


    @FXML
    private Label scannedResultLabel;


    @FXML
    private Label warningMessage;



    private boolean isCameraRunning = false;

    private Thread cameraThread;



    // SERVICES

    private WebCamService webCamService;

    private QRScannerService qrService;

    private ImageSaveService saveService;

    private ProductSearchService searchService;



    @FXML
    public void initialize() {


        warningMessage.setText(
                "Ready to query products."
        );


        webCamService = new WebCamService();

        qrService = new QRScannerService();

        saveService = new ImageSaveService();

        searchService = new ProductSearchService();

    }





    @FXML
    public void handleScanProduct(ActionEvent event) {


        if(isCameraRunning){

            stopWebcam();

            warningMessage.setText(
                    "Camera stopped."
            );

            return;
        }



        warningMessage.setText(
                "Starting camera... Scan QR code."
        );



        isCameraRunning = true;



        cameraThread =
                new Thread(this::runWebcamStream);



        cameraThread.setDaemon(true);

        cameraThread.start();

    }


    private void runWebcamStream(){


        try{


            webCamService.start();



            while(isCameraRunning){


                BufferedImage frame =
                        webCamService.getFrame();



                if(frame != null){



                    Image fxImage =
                            SwingFXUtils
                            .toFXImage(frame, null);



                    Platform.runLater(() ->
                            cameraView.setImage(fxImage)
                    );



                    String qrData =
                            qrService.scan(frame);



                    if(qrData != null){



                        stopWebcam();



                        String savedPath =
                                saveService.save(
                                        frame,
                                        qrData
                                );



                        Platform.runLater(() ->
                                processScannedProduct(
                                        qrData,
                                        savedPath
                                )
                        );


                        break;

                    }

                }


                Thread.sleep(50);


            }



        }catch(Exception e){


            e.printStackTrace();


            Platform.runLater(() ->
                    warningMessage.setText(
                            "Camera error: "
                            + e.getMessage()
                    )
            );

        }

    }







    private void processScannedProduct(
            String productCode,
            String savedPath
    ){


        FoodItem item =
                searchService.search(productCode);



        if(item != null){


            warningMessage.setText(
                    "Scan Successful!"
            );


            scannedResultLabel.setText(
                    "Product Found: "
                    + item.getName()
                    + " (ID: "
                    + item.getID()
                    + ")"
            );



        }
        else{


            scannedResultLabel.setText(
                    "Scanned Data: "
                    + productCode
            );


            warningMessage.setText(
                    "No matching product found."
            );


        }


    }







    @FXML
    public void handleEnterPlu(ActionEvent event){


        stopWebcam();



        TextInputDialog dialog =
                new TextInputDialog();



        dialog.setTitle(
                "Manual Search"
        );


        dialog.setHeaderText(
                "Manual Product Query"
        );


        dialog.setContentText(
                "Enter Ingredient ID or Name:"
        );



        Optional<String> result =
                dialog.showAndWait();



        result.ifPresent(
                code ->
                processScannedProduct(
                        code,
                        "Manual Search"
                )
        );


    }







    @FXML
    public void handleChooseFrom(ActionEvent event){


        stopWebcam();



        var ingredients =
                FoodItemDAO.getAllFoodIngredients();



        if(ingredients.isEmpty()){


            warningMessage.setText(
                    "No ingredients found."
            );


            return;

        }



        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );



        alert.setTitle(
                "Ingredient Database"
        );


        alert.setHeaderText(
                "Available Ingredients"
        );



        StringBuilder builder =
                new StringBuilder();



        ingredients.forEach(item ->
                builder.append("• ")
                        .append(
                                item.getName()
                        )
                        .append("\n")
        );



        alert.setContentText(
                builder.toString()
        );


        alert.showAndWait();

    }







    public void stopWebcam(){


        isCameraRunning = false;


        if(webCamService != null){

            webCamService.stop();

        }


    }



}