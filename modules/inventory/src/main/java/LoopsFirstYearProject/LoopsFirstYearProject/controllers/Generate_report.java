package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import dao.IngredientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Ingredient;
import services.InventoryReportService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Generate_report {

    @FXML private ImageView logo;
    @FXML private TableView<Ingredient> tableview;
    @FXML private TableColumn<Ingredient, String> ingredientID;
    @FXML private TableColumn<Ingredient, String> ingredientName;
    @FXML private TableColumn<Ingredient, Integer> stockQuantity;
    @FXML private TableColumn<Ingredient, String> warehouseID;
    @FXML private TableColumn<Ingredient, Integer> capacity;
    @FXML private ComboBox<String> year;
    @FXML private Label reportStatus;

    @FXML
    public void initialize() {
        logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        tableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ingredientID.setCellValueFactory(new PropertyValueFactory<>("ingredientID"));
        ingredientName.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        stockQuantity.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        warehouseID.setCellValueFactory(new PropertyValueFactory<>("warehouseID"));
        capacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        year.getItems().setAll("2022", "2023", "2024", "2025", "2026");
        year.setValue("2026");
        loadIngredients();
        year.setOnAction(event -> loadIngredients());
    }

    private void loadIngredients() {
        String selectedYear = year.getValue();
        if (selectedYear != null) {
            tableview.setItems(IngredientDAO.getAllIngredients("Ingredient" + selectedYear));
            reportStatus.setText(tableview.getItems().size() + " stock records loaded for "
                    + selectedYear + ". Click a column heading to sort.");
        }
    }

    @FXML
    public void refresh() {
        loadIngredients();
    }

    @FXML
    private void report() {
        String selectedYear = year.getValue();
        if (selectedYear == null) {
            showAlert(Alert.AlertType.WARNING, "Select Year", "Select a year first.");
            return;
        }
        if (tableview.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Report Data",
                    "There are no inventory records for " + selectedYear + ".");
            return;
        }

        try {
            Path reportsDirectory = DBConnection.getDatabasePath().getParent().resolve("reports");
            Files.createDirectories(reportsDirectory);

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Inventory Report");
            chooser.setInitialDirectory(reportsDirectory.toFile());
            chooser.setInitialFileName("Inventory_Report_" + selectedYear + ".pdf");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF documents", "*.pdf"));

            File selectedFile = chooser.showSaveDialog(tableview.getScene().getWindow());
            if (selectedFile == null) {
                reportStatus.setText("Report export cancelled.");
                return;
            }

            Path generated = InventoryReportService.generate(
                    selectedFile.toPath(), selectedYear, tableview.getItems());
            reportStatus.setText("Saved: " + generated);
            showAlert(Alert.AlertType.INFORMATION, "Report Generated",
                    "Inventory report saved to:\n" + generated);
        } catch (Exception exception) {
            reportStatus.setText("Report failed: " + exception.getMessage());
            showAlert(Alert.AlertType.ERROR, "Report Failed",
                    "The report could not be created.\n" + exception.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
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
