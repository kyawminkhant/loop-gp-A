package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import dao.FoodItemDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.FoodItem;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import LoopsFirstYearProject.LoopsFirstYearProject.App;

public class FoodCardController implements Initializable {

    @FXML
    private TilePane productGrid;

    @FXML
    private ImageView logo;
     
    @FXML
    private Button Home;
    
    @FXML
    private Button openAddStock;
    
    @FXML
    private Button profileBtn;
     
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField searchBar;

    private List<FoodItem> foodItems = new ArrayList<>();
    private final ContextMenu suggestionPopup = new ContextMenu();
     
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
         
        productGrid.prefWidthProperty().bind(scrollPane.widthProperty());

        loadDataFromDatabase();
        displayFoodItems(foodItems);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFoodItems(newValue);
            updateSuggestions(newValue);
        });

        searchBar.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                suggestionPopup.hide();
            }
        });
    }

    private void filterFoodItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            displayFoodItems(foodItems);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase().trim();
        List<FoodItem> filteredList = new ArrayList<>();

        for (FoodItem item : foodItems) {
            boolean matchesName = item.getName() != null && item.getName().toLowerCase().contains(lowerCaseFilter);
            boolean matchesId = String.valueOf(item.getID()).contains(lowerCaseFilter);

            if (matchesName || matchesId) {
                filteredList.add(item);
            }
        }

        displayFoodItems(filteredList);
    }

    private void updateSuggestions(String searchText) {
        suggestionPopup.getItems().clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            suggestionPopup.hide();
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase().trim();
        List<FoodItem> matches = new ArrayList<>();

        for (FoodItem item : foodItems) {
            if (item.getName() != null && item.getName().toLowerCase().contains(lowerCaseFilter)) {
                matches.add(item);
            }
            if (matches.size() >= 6) { 
                break;
            }
        }

        if (matches.isEmpty()) {
            suggestionPopup.hide();
            return;
        }

        for (FoodItem match : matches) {
            MenuItem menuItem = new MenuItem(match.getName());
            menuItem.setOnAction(e -> {
                searchBar.setText(match.getName());
                searchBar.positionCaret(searchBar.getText().length());
                suggestionPopup.hide();
            });
            suggestionPopup.getItems().add(menuItem);
        }

        if (!suggestionPopup.isShowing()) {
            suggestionPopup.show(searchBar, Side.BOTTOM, 0, 5);
        }
    }
     
    private void displayFoodItems(List<FoodItem> items) {
        productGrid.getChildren().clear();

        for (FoodItem item : items) {
            try {
                // 1. Load the UI layout card for the single food item
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlFiles/singleFoodItem.fxml"));
                VBox card = loader.load();

                // 2. Set card details using its controller
                SingleFoodItemController controller = loader.getController();
                controller.setFoodData(item);

                // 3. ATTACH CLICK LISTENER DIRECTLY ON THE CARD (VBox card)
                card.setOnMouseClicked(event -> {
                    try {
                        FXMLLoader popupLoader = new FXMLLoader(getClass().getResource("/LoopsFirstYearProject/LoopsFirstYearProject/views/FoodItemDetailPopup.fxml"));
                        Parent root = popupLoader.load();

                        // Access the Detail Controller to populate data
                        FoodItemDetailPopupController popupController = popupLoader.getController();
                        
                        // Default Fallbacks - update if food item properties exist
                        String category = "Menu Item"; 
                        int stock = 1;                 
                        String imagePath = "";         
                        
                        popupController.setFoodData(
                            String.valueOf(item.getID()),
                            item.getName(),
                            category, 
                            stock,    
                            imagePath 
                        );

                        // Open modal stage
                        Stage stage = new Stage();
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setTitle("Food Details - " + item.getName());
                        stage.setScene(new Scene(root));
                        stage.showAndWait();

                    } catch (IOException e) {
                        System.err.println("Could not load details popup FXML.");
                        e.printStackTrace();
                    }
                });

                // Add card to grid
                productGrid.getChildren().add(card);

            } catch (IOException e) {
                System.err.println("Error loading single food item card.");
                e.printStackTrace();
            }
        }
    }
     
    private void loadDataFromDatabase() {
        FoodItemDAO foodItemDAO = new FoodItemDAO();
        this.foodItems = foodItemDAO.getAllFoodItems();

        System.out.println("Items loaded: " + foodItems.size());
        for (FoodItem item : foodItems) {
            System.out.println(item.getID() + " - " + item.getName());
        }
    }
     
    @FXML
    private void Home() throws IOException {
        App.setRoot("dashboard");
    }
    
    @FXML
    private void profileBtn() throws IOException {
        System.out.println("button clicked");
    }
    
    @FXML
    public void openAddStock(){

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


            stage.setTitle("Add Stock");


            stage.setScene(
                    new Scene(root)
            );


            stage.showAndWait();


        }catch(Exception e){

            e.printStackTrace();

        }

    }
    
    @FXML
    private void updateProducts(){

        loadDataFromDatabase();

        displayFoodItems(foodItems);


        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Updated");

        alert.setHeaderText(null);

        alert.setContentText(
            "Product list updated successfully"
        );

        alert.showAndWait();

    }
    
}