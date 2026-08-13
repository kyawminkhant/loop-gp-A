package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import model.WarehouseDelivery;
import services.InventoryDeliveryService;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Displays the live inbound-delivery queue maintained by Inventory. */
public final class WarehouseDeliveryController {

    @FXML private ImageView logo;
    @FXML private ComboBox<String> warehouseFilter;
    @FXML private Label queueLabel;
    @FXML private Label updateLabel;
    @FXML private TableView<WarehouseDelivery> deliveryTable;
    @FXML private TableColumn<WarehouseDelivery, Integer> deliveryIdColumn;
    @FXML private TableColumn<WarehouseDelivery, String> warehouseColumn;
    @FXML private TableColumn<WarehouseDelivery, String> areaColumn;
    @FXML private TableColumn<WarehouseDelivery, String> ingredientColumn;
    @FXML private TableColumn<WarehouseDelivery, Integer> quantityColumn;
    @FXML private TableColumn<WarehouseDelivery, String> statusColumn;
    @FXML private TableColumn<WarehouseDelivery, String> expectedColumn;
    @FXML private TableColumn<WarehouseDelivery, String> updatedColumn;

    private final Timeline refreshTimer = new Timeline();

    @FXML
    public void initialize() {
        logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        deliveryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        deliveryIdColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryID"));
        warehouseColumn.setCellValueFactory(new PropertyValueFactory<>("warehouseID"));
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("serviceArea"));
        ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        expectedColumn.setCellValueFactory(new PropertyValueFactory<>("expectedAt"));
        updatedColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        warehouseFilter.getItems().add("All warehouses");
        try {
            warehouseFilter.getItems().addAll(
                    InventoryDeliveryService.loadWarehouseChoices());
            warehouseFilter.getSelectionModel().selectFirst();
            InventoryDeliveryService.startAutomaticUpdates();
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }

        warehouseFilter.setOnAction(event -> refresh());
        refreshTimer.getKeyFrames().setAll(
                new KeyFrame(Duration.seconds(2), event -> {
                    if (deliveryTable.getScene() == App.getScene()) {
                        refresh();
                    } else {
                        refreshTimer.stop();
                    }
                }));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }

    @FXML
    private void createDelivery() {
        try {
            int deliveryId = InventoryDeliveryService.createRandomDelivery();
            updateLabel.setText("Created delivery #" + deliveryId + ".");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void advanceDelivery() {
        try {
            InventoryDeliveryService.runRandomUpdate();
            updateLabel.setText("Delivery queue updated manually.");
            refresh();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    @FXML
    private void back() throws IOException {
        refreshTimer.stop();
        App.setRoot("dashboard");
    }

    private void refresh() {
        try {
            String selected = warehouseFilter.getValue();
            String filter = selected == null || "All warehouses".equals(selected)
                    ? "" : selected;
            deliveryTable.setItems(FXCollections.observableArrayList(
                    InventoryDeliveryService.loadDeliveries(filter)));
            int active = InventoryDeliveryService.countActiveDeliveries();
            queueLabel.setText(active + " active inbound deliveries");
            if (updateLabel.getText() == null || updateLabel.getText().isBlank()) {
                updateLabel.setText("Automatic stock updates are running. Last checked "
                        + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void showError(Exception exception) {
        updateLabel.setText("Could not update deliveries: " + exception.getMessage());
        updateLabel.setStyle("-fx-text-fill: #a52a2a;");
    }
}
