package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import LoopsFirstYearProject.LoopsFirstYearProject.App;
import dao.LocationsDAO;
import model.StorageLocations;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class StorageLocationsController {

	@FXML
	private ImageView logo;
	
	@FXML
	private TableView <StorageLocations> storageLocationsTable;

	@FXML
	private TableColumn <StorageLocations, String> name;

	@FXML
	private TableColumn <StorageLocations, String> locations;
	
	@FXML
	private TableColumn <StorageLocations, Integer> locationID;
	
	@FXML
	private TableColumn <StorageLocations, Integer> capacity;
	
	@FXML
	private TableColumn <StorageLocations, Integer> currentStock;
	
	@FXML
	private TableColumn <StorageLocations, Double> usage;
	
	@FXML
	private TableColumn <StorageLocations, String> manager;
	
	@SuppressWarnings("deprecation")
	@FXML
	public void initialize(){
		logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
		storageLocationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		name.setCellValueFactory(new PropertyValueFactory<>("name"));
		locations.setCellValueFactory(new PropertyValueFactory<>("locations"));
		locationID.setCellValueFactory(new PropertyValueFactory<>("locationID"));
		capacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
		currentStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
		usage.setCellValueFactory(new PropertyValueFactory<>("usage"));
		manager.setCellValueFactory(new PropertyValueFactory<>("manager"));
		storageLocationsTable.setItems(LocationsDAO.getStorageLocations("moreStorageLocations"));
	}	  
	
	@FXML 
	private void moreStorage() {
		storageLocationsTable.setItems(LocationsDAO.getStorageLocations("live"));
	}

	@FXML
	private void back() {
		try {
			App.setRoot("analytics");
		} catch (Exception exception) {
			throw new IllegalStateException("Could not return to Inventory Analytics.", exception);
		}
	}
}
