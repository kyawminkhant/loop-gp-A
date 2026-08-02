package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import dao.IngredientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Ingredient;

import java.io.File;
import java.io.FileOutputStream;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class Generate_report {

    @FXML
    private ImageView logo;

    @FXML
    private ImageView profileIcon;
    
    @FXML
    private Button back;

    @FXML
    private TableView<Ingredient> tableview;

    @FXML
    private TableColumn<Ingredient,String> ingredientID;

    @FXML
    private TableColumn<Ingredient,String> ingredientName;

    @FXML
    private TableColumn<Ingredient,Integer> stockQuantity;

    @FXML
    private TableColumn<Ingredient,String> warehouseID;

    @FXML
    private TableColumn<Ingredient,Integer> capacity;

    @FXML
    private ComboBox<String> year;



    @SuppressWarnings("deprecation")
	@FXML
    public void initialize(){

        logo.setImage(
            new Image(
                getClass()
                .getResourceAsStream("/images/logo.png")
            )
        );

        profileIcon.setImage(
            new Image(
                getClass()
                .getResourceAsStream("/images/user.png")
            )
        );


        tableview.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );


        ingredientID.setCellValueFactory(
            new PropertyValueFactory<>("ingredientID")
        );

        ingredientName.setCellValueFactory(
            new PropertyValueFactory<>("ingredientName")
        );

        stockQuantity.setCellValueFactory(
            new PropertyValueFactory<>("stockQuantity")
        );

        warehouseID.setCellValueFactory(
            new PropertyValueFactory<>("warehouseID")
        );

        capacity.setCellValueFactory(
            new PropertyValueFactory<>("capacity")
        );


        year.getItems()
            .addAll(
                "2022",
                "2023",
                "2024",
                "2025",
                "2026"
            );


        year.setValue("2026");


        loadIngredients("Ingredient2026");


        year.setOnAction(e -> {

            String selected =
                    year.getValue();

            if(selected != null){

                loadIngredients(
                    "Ingredient" + selected
                );

            }

        });

    }



    private void loadIngredients(String tableName){

        tableview.setItems(
            IngredientDAO
            .getAllIngredients(tableName)
        );

    }



    // Used after Add / Update / Delete

    public void refresh(){

        loadIngredients(
            "Ingredient" + year.getValue()
        );

    }



    @FXML
    private void addStock(){

        // We will connect AddStock.fxml popup here

    }



    @FXML
    private void updateStock(){

        // Update popup here

    }



    @FXML
    private void deleteStock(){

        // Delete logic here

    }



    @FXML
    private void report(){

        String selectedYear =
                year.getValue();


        if(selectedYear == null){

            showAlert(
                Alert.AlertType.WARNING,
                "Warning",
                "Select year first"
            );

            return;
        }


        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Save Inventory Report"
        );


        chooser.setInitialFileName(
                "Inventory_Report_" 
                + selectedYear 
                + ".pdf"
        );


        File file =
                chooser.showSaveDialog(
                    tableview
                    .getScene()
                    .getWindow()
                );


        if(file != null){

            generatePdf(
                file,
                selectedYear
            );

        }

    }



    private void generatePdf(
            File file,
            String year
    ){

        try{

            Document document =
                    new Document();


            PdfWriter.getInstance(
                document,
                new FileOutputStream(file)
            );


            document.open();

         // Add application logo to PDF
            try {

                com.lowagie.text.Image pdfLogo =
                        com.lowagie.text.Image.getInstance(
                                getClass()
                                .getResource("/images/logo.png")
                        );

                // Same idea as your JavaFX ImageView size
                pdfLogo.scaleToFit(120, 120);

                // Position: top-left like your application sidebar/logo area
                pdfLogo.setAlignment(
                        com.lowagie.text.Image.ALIGN_LEFT
                );

                document.add(pdfLogo);


            } catch(Exception e) {

                e.printStackTrace();

            }

            document.add(
                new Paragraph(
                    "Inventory Report - "
                    + year
                )
            );


            PdfPTable table =
                    new PdfPTable(5);


            table.addCell("ID");
            table.addCell("Name");
            table.addCell("Stock");
            table.addCell("Warehouse");
            table.addCell("Capacity");


            for(Ingredient i :
                    tableview.getItems()){


                table.addCell(
                    i.getIngredientID()
                );

                table.addCell(
                    i.getIngredientName()
                );

                table.addCell(
                    String.valueOf(
                        i.getStockQuantity()
                    )
                );

                table.addCell(
                    i.getWarehouseID()
                );

                table.addCell(
                    String.valueOf(
                        i.getCapacity()
                    )
                );

            }


            document.add(table);

            document.close();


            showAlert(
                Alert.AlertType.INFORMATION,
                "Success",
                "Report generated"
            );


        }catch(Exception e){

            e.printStackTrace();

        }

    }



    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ){

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();

    }




}