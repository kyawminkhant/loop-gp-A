package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import LoopsFirstYearProject.LoopsFirstYearProject.App;
import Utils.ProfileMenu;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import java.io.*;

public class Dashboard {

	@FXML
	private ImageView logo;
	
	@FXML
	private Button back;
	
	@FXML
	private ImageView profileIcon;
	
	@FXML
	private ImageView add_stock;
	
	@FXML
	private ImageView manage_stock;
	
	@FXML
	private ImageView analytics;
	
	@FXML
	private ImageView product_query;
	
	@FXML
	private ImageView generate_report;

	@FXML
	private ImageView inventory_deliveries;

	
	@FXML
	public void initialize() {
		logo.setImage(
		        new Image(getClass().getResourceAsStream("/images/logo.png"))
		    );
		
		profileIcon.setImage(
		        new Image(
		            getClass().getResourceAsStream("/images/user.png")
		        )
		    );

		    ProfileMenu.attach(profileIcon);
		    
	    add_stock.setImage(
	        new Image(getClass().getResourceAsStream("/images/add_stock.png"))
	    );
	    manage_stock.setImage(
		        new Image(getClass().getResourceAsStream("/images/manage_stock.png"))
		    );
	    product_query.setImage(
		        new Image(getClass().getResourceAsStream("/images/product_query.png"))
		    );
	    analytics.setImage(
		        new Image(getClass().getResourceAsStream("/images/analytics.png"))
		    );
	    generate_report.setImage(
		        new Image(getClass().getResourceAsStream("/images/generate_report.png"))
		    );
	    inventory_deliveries.setImage(
		        new Image(getClass().getResourceAsStream("/images/manage_stock.png"))
		    );
	    }
	    
	    
	    @FXML
	    public void Product_dashboard() throws IOException{
	    App.setRoot("product_dashboard");
	    	}
	    
	    @FXML
	    public void GenerateReport() throws IOException{
	    App.setRoot("generateReport");
	    	}
	    
	    @FXML
	    public void Analytics() throws IOException{
	    App.setRoot("analytics");
	    	}
	    
	    @FXML
	    public void ManageStock() throws IOException{
	    App.setRoot("manageStock");
	    	}
	    
    @FXML
    private void ProductQuery() throws IOException {
        App.setRoot("product_query");
    }

	@FXML
	private void WarehouseDeliveries() throws IOException {
		App.setRoot("warehouseDeliveries");
	}
	    
	}


