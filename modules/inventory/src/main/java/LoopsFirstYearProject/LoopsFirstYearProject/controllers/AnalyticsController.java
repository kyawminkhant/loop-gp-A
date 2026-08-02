package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import LoopsFirstYearProject.LoopsFirstYearProject.App;
import dao.AnalyticsDAO;
import javafx.scene.image.ImageView;
import model.Analytics;
import javafx.fxml.*;
import java.io.*;


public class AnalyticsController {

	@FXML
	private ImageView logo;
	
	@FXML
	private Button Back;
	
	@FXML
	private ImageView profileIcon;
	
	@FXML
	private TableView <Analytics> analytics; 
	
	@FXML
	private TableColumn<Analytics, String> product;
	
	@FXML
	private TableColumn<Analytics, String> locationColumn; 
	
	@FXML
	private TableColumn<Analytics, Integer> currentQuantity;
	
	@FXML
	private TableColumn<Analytics, Integer> minThreshold;
	
	@FXML
	private TableColumn<Analytics, String> status;
	
	@FXML
	private TableColumn<Analytics, String> lastRestock;
	
	@FXML
	private TableColumn<Analytics, Integer> daysUntilReorder;
	
	String Good = "GOOD";
	String Low = "LOW";
	String Critical = "CRITICAL";

	
	
	@SuppressWarnings("deprecation")
	@FXML
	public void initialize() {
		
		analytics.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));	
		profileIcon.setImage(
			    new Image(getClass().getResourceAsStream("/images/user.png"))
			);

		analytics.setItems(AnalyticsDAO.getLocations());
		product.setCellValueFactory(new PropertyValueFactory<>("product"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
		currentQuantity.setCellValueFactory(new PropertyValueFactory<>("currentQuantity"));
		minThreshold.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		lastRestock.setCellValueFactory(new PropertyValueFactory<>("lastRestock"));
		daysUntilReorder.setCellValueFactory(new PropertyValueFactory<>("daysUntilReorder"));
		}		
	
	
	public void moreLocations() throws IOException{
		App.setRoot("storageLocations");
	}
	
	public void back() throws IOException {
		App.setRoot("dashboard");
	}
	
}

