package LoopsFirstYearProject.LoopsFirstYearProject.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.image.*;
import LoopsFirstYearProject.LoopsFirstYearProject.App;
import dao.AnalyticsDAO;
import javafx.scene.image.ImageView;
import model.Analytics;
import javafx.fxml.*;
import java.io.*;
import java.util.Locale;


public class AnalyticsController {

	@FXML
	private ImageView logo;
	
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

	@FXML
	private TextField searchField;

	@FXML
	private ComboBox<String> statusFilter;

	@FXML
	private Label totalRows;

	@FXML
	private Label locationCount;

	@FXML
	private Label lowCount;

	@FXML
	private Label criticalCount;

	@FXML
	private Label analyticsMessage;

	private final ObservableList<Analytics> allRows = FXCollections.observableArrayList();
	private FilteredList<Analytics> filteredRows;
	
	@FXML
	public void initialize() {
		analytics.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		logo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));	
		if (profileIcon != null) {
			profileIcon.setImage(new Image(getClass().getResourceAsStream("/images/user.png")));
		}

		product.setCellValueFactory(new PropertyValueFactory<>("product"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
		currentQuantity.setCellValueFactory(new PropertyValueFactory<>("currentQuantity"));
		minThreshold.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		lastRestock.setCellValueFactory(new PropertyValueFactory<>("lastRestock"));
		daysUntilReorder.setCellValueFactory(new PropertyValueFactory<>("daysUntilReorder"));
		status.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : item);
				getStyleClass().removeAll(
						"analytics-status-available", "analytics-status-low", "analytics-status-critical");
				if (!empty && item != null) {
					getStyleClass().add(switch (item.toUpperCase(Locale.ROOT)) {
						case "CRITICAL" -> "analytics-status-critical";
						case "LOW" -> "analytics-status-low";
						default -> "analytics-status-available";
					});
				}
			}
		});

		statusFilter.setItems(FXCollections.observableArrayList(
				"All statuses", "Critical", "Low", "Available"));
		statusFilter.getSelectionModel().selectFirst();
		searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
		statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());
		refresh();
	}

	@FXML
	private void refresh() {
		try {
			allRows.setAll(AnalyticsDAO.getLocations());
			filteredRows = new FilteredList<>(allRows, row -> true);
			SortedList<Analytics> sortedRows = new SortedList<>(filteredRows);
			sortedRows.comparatorProperty().bind(analytics.comparatorProperty());
			analytics.setItems(sortedRows);
			updateSummary();
			applyFilter();
			analyticsMessage.setText("Live figures from the latest Inventory stock year.");
		} catch (RuntimeException exception) {
			allRows.clear();
			analytics.setItems(FXCollections.observableArrayList());
			analyticsMessage.setText("Analytics could not be loaded: "
					+ rootMessage(exception));
		}
	}

	private void applyFilter() {
		if (filteredRows == null) {
			return;
		}
		String search = searchField.getText() == null
				? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
		String selectedStatus = statusFilter.getValue() == null
				? "All statuses" : statusFilter.getValue();
		filteredRows.setPredicate(row -> {
			boolean matchesText = search.isEmpty()
					|| row.getProduct().toLowerCase(Locale.ROOT).contains(search)
					|| row.getLocation().toLowerCase(Locale.ROOT).contains(search);
			boolean matchesStatus = selectedStatus.startsWith("All")
					|| row.getStatus().equalsIgnoreCase(selectedStatus);
			return matchesText && matchesStatus;
		});
		analyticsMessage.setText(String.format(Locale.UK,
				"Showing %,d of %,d live stock records.", filteredRows.size(), allRows.size()));
	}

	private void updateSummary() {
		totalRows.setText(String.format(Locale.UK, "%,d", allRows.size()));
		locationCount.setText(Long.toString(allRows.stream()
				.map(Analytics::getLocation).distinct().count()));
		lowCount.setText(Long.toString(allRows.stream()
				.filter(row -> "LOW".equalsIgnoreCase(row.getStatus())).count()));
		criticalCount.setText(Long.toString(allRows.stream()
				.filter(row -> "CRITICAL".equalsIgnoreCase(row.getStatus())).count()));
	}

	private String rootMessage(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null) {
			current = current.getCause();
		}
		return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
	}
	
	
	public void moreLocations() throws IOException{
		App.setRoot("storageLocations");
	}
	
	public void back() throws IOException {
		App.setRoot("dashboard");
	}

	@FXML
	private void replenish() throws IOException {
		App.setRoot("product_dashboard");
	}
	
}

