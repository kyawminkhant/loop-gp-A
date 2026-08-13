package controllers;

import database.ChefReviewDAO;
import database.CustomerDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.*;
import utils.AnimationUtil;
import utils.ImageFileUtil;
import utils.NavigationUtil;
import utils.SessionManager;
import utils.ValidationUtil;
import services.InventoryDeliveryService;

import java.util.List;
import java.util.Random;

public class DashboardController {

    @FXML private TabPane mainTabPane;
    @FXML private Tab preferencesTab;
    @FXML private Tab orderHistoryTab;
    @FXML private Tab chefReviewsTab;
    @FXML private ImageView headerLogo;

    @FXML private Label profileNameDisplayLabel;
    @FXML private TextField profileNameField;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileIdCardLabel;
    @FXML private ImageView profileIdCardImage;
    @FXML private Label profileStatusLabel;
    @FXML private TextField profileMobileField;
    @FXML private TextField profileAddressField;
    @FXML private Label profileWarehouseLabel;
    @FXML private Label profileMessageLabel;

    @FXML private CheckBox prefVegan;
    @FXML private CheckBox prefVegetarian;
    @FXML private CheckBox prefKeto;
    @FXML private CheckBox prefGlutenFree;
    @FXML private CheckBox prefHalal;
    @FXML private CheckBox prefLowCalorie;
    @FXML private CheckBox prefPescatarian;
    @FXML private CheckBox prefHighProtein;
    @FXML private CheckBox prefWeightLoss;
    @FXML private CheckBox notificationsCheckBox;
    @FXML private TextArea deliveryInstructionsArea;
    @FXML private Label prefMessageLabel;

    @FXML private Label totalOrdersLabel;
    @FXML private Label totalSpentLabel;
    @FXML private TableView<OrderHistoryItem> orderTable;
    @FXML private TableColumn<OrderHistoryItem, String> orderIdColumn;
    @FXML private TableColumn<OrderHistoryItem, String> orderDateColumn;
    @FXML private TableColumn<OrderHistoryItem, String> orderStatusColumn;
    @FXML private TableColumn<OrderHistoryItem, Double> orderTotalColumn;

    @FXML private ComboBox<Chef> chefComboBox;
    @FXML private ComboBox<String> ratingComboBox;
    @FXML private TextArea reviewTextArea;
    @FXML private Label reviewMessageLabel;
    @FXML private Label selectedChefNameLabel;
    @FXML private Label selectedChefSpecialityLabel;
    @FXML private Label selectedChefRatingLabel;
    @FXML private Label chefCommentsLabel;
    @FXML private TableView<ChefReview> chefCommentsTable;
    @FXML private TableColumn<ChefReview, String> chefCommentReviewerColumn;
    @FXML private TableColumn<ChefReview, String> chefCommentRatingColumn;
    @FXML private TableColumn<ChefReview, String> chefCommentTextColumn;
    @FXML private TableColumn<ChefReview, String> chefCommentDateColumn;
    @FXML private TableView<ChefReview> reviewTable;
    @FXML private TableColumn<ChefReview, String> reviewChefNameColumn;
    @FXML private TableColumn<ChefReview, String> reviewRatingColumn;
    @FXML private TableColumn<ChefReview, String> reviewTextColumn;
    @FXML private TableColumn<ChefReview, String> reviewDateColumn;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ChefReviewDAO chefReviewDAO = new ChefReviewDAO();

    private Customer currentCustomer;
    private Timeline notificationTimer;
    private final Random random = new Random();
    private boolean preferencesLoaded;
    private boolean ordersLoaded;
    private boolean chefReviewsLoaded;

    @FXML
    public void initialize() {
        currentCustomer = SessionManager.getCurrentCustomer();

        if (currentCustomer == null) {
            redirectToLogin();
            return;
        }

        loadProfile();
        startNotificationTimer();
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, selectedTab) -> loadTabWhenNeeded(selectedTab));
    }

    private void loadTabWhenNeeded(Tab selectedTab) {
        if (selectedTab == preferencesTab && !preferencesLoaded) {
            loadPreferences();
            preferencesLoaded = true;
        } else if (selectedTab == orderHistoryTab && !ordersLoaded) {
            setupOrderTable();
            loadOrderHistory();
            ordersLoaded = true;
        } else if (selectedTab == chefReviewsTab && !chefReviewsLoaded) {
            setupChefReviews();
            loadMyReviews();
            chefReviewsLoaded = true;
        }
    }

    private void loadProfile() {
        profileNameDisplayLabel.setText(currentCustomer.getName());
        profileNameField.setText(currentCustomer.getName());
        profileEmailLabel.setText(currentCustomer.getEmail());
        profileIdCardLabel.setText(currentCustomer.getIdCardNo());
        profileStatusLabel.setText(currentCustomer.getStatus());
        profileMobileField.setText(currentCustomer.getMobile());
        profileAddressField.setText(currentCustomer.getDeliveryAddress());
        updateWarehouseAssignment();

        profileIdCardImage.setImage(ImageFileUtil.loadImage(currentCustomer.getIdCardImagePath()));

        boolean active = "Active".equalsIgnoreCase(currentCustomer.getStatus());
        String colour = active ? "#2f6b3f" : "#8c2f1f";

        profileStatusLabel.setStyle("-fx-text-fill: " + colour + "; -fx-font-weight: bold;");
    }

    private void redirectToLogin() {
        Platform.runLater(() -> {
            if (profileNameField != null && profileNameField.getScene() != null) {
                Stage stage = (Stage) profileNameField.getScene().getWindow();
                NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
            }
        });
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String name = profileNameField.getText().trim();
        String mobile = profileMobileField.getText().trim();
        String address = profileAddressField.getText().trim();

        if (!ValidationUtil.isNotEmpty(name)) {
            profileMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            profileMessageLabel.setText("Name cannot be empty.");
            AnimationUtil.shake(profileMessageLabel);
            return;
        }

        if (ValidationUtil.containsUnsafeCharacters(name)) {
            profileMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            profileMessageLabel.setText("Name cannot contain special characters like @#/\\?\"'~`$");
            AnimationUtil.shake(profileMessageLabel);
            return;
        }

        if (!ValidationUtil.isValidMobile(mobile)) {
            profileMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            profileMessageLabel.setText("Mobile must be 10-15 digits.");
            AnimationUtil.shake(profileMessageLabel);
            return;
        }

        if (!ValidationUtil.isNotEmpty(address)) {
            profileMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            profileMessageLabel.setText("Delivery address cannot be empty.");
            AnimationUtil.shake(profileMessageLabel);
            return;
        }

        if (ValidationUtil.containsUnsafeCharacters(address)) {
            profileMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            profileMessageLabel.setText("Address cannot contain special characters like @#/\\?\"'~`$");
            AnimationUtil.shake(profileMessageLabel);
            return;
        }

        boolean success = customerDAO.updateProfile(
                currentCustomer.getCustomerID(),
                currentCustomer.getPersonID(),
                name,
                mobile,
                address
        );

        if (success) {
            currentCustomer.setName(name);
            currentCustomer.setMobile(mobile);
            currentCustomer.setDeliveryAddress(address);
            SessionManager.setCurrentCustomer(currentCustomer);
            profileNameDisplayLabel.setText(name);
            updateWarehouseAssignment();

            profileMessageLabel.setStyle("-fx-text-fill: #2f6b3f;");
            profileMessageLabel.setText("Profile updated successfully.");
            AnimationUtil.pulse(profileMessageLabel);
        } else {
            profileMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            profileMessageLabel.setText("Update failed. Please try again.");
            AnimationUtil.shake(profileMessageLabel);
        }
    }

    private void updateWarehouseAssignment() {
        try {
            InventoryDeliveryService.WarehouseAssignment warehouse =
                    InventoryDeliveryService.resolveWarehouse(
                            currentCustomer.getDeliveryAddress());
            profileWarehouseLabel.setText("Local fulfilment: "
                    + warehouse.getDisplayName());
        } catch (Exception exception) {
            profileWarehouseLabel.setText("Local fulfilment warehouse unavailable");
        }
    }

    @FXML
    private void handleDeactivateAccount(ActionEvent event) {
        Alert firstConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        firstConfirm.setTitle("Deactivate Account");
        firstConfirm.setHeaderText(null);
        firstConfirm.setContentText("Are you sure you want to deactivate your account?");

        firstConfirm.showAndWait().ifPresent(firstResponse -> {
            if (firstResponse == ButtonType.OK) {
                Alert secondConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                secondConfirm.setTitle("Confirm Deactivation");
                secondConfirm.setHeaderText(null);
                secondConfirm.setContentText("This will prevent future logins. Confirm?");

                secondConfirm.showAndWait().ifPresent(secondResponse -> {
                    if (secondResponse == ButtonType.OK) {
                        boolean success = customerDAO.deactivateCustomer(currentCustomer.getCustomerID());

                        if (success) {
                            stopNotificationTimer();
                            SessionManager.clear();

                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
                        }
                    }
                });
            }
        });
    }

    private void loadPreferences() {
        CustomerPreference pref = customerDAO.getPreference(currentCustomer.getCustomerID());

        if (pref == null) {
            return;
        }

        String categories = pref.getFavoriteCategories() == null ? "" : pref.getFavoriteCategories();

        prefVegan.setSelected(categories.contains("Vegan"));
        prefVegetarian.setSelected(categories.contains("Vegetarian"));
        prefKeto.setSelected(categories.contains("Keto"));
        prefGlutenFree.setSelected(categories.contains("Gluten-Free"));
        prefHalal.setSelected(categories.contains("Halal"));
        prefLowCalorie.setSelected(categories.contains("Low-Calorie"));
        prefPescatarian.setSelected(categories.contains("Pescatarian"));
        prefHighProtein.setSelected(categories.contains("High-Protein"));
        prefWeightLoss.setSelected(categories.contains("Weight-Loss"));

        notificationsCheckBox.setSelected("Enabled".equalsIgnoreCase(pref.getNotificationSettings()));
        deliveryInstructionsArea.setText(
                pref.getDeliveryInstructions() == null ? "" : pref.getDeliveryInstructions()
        );
    }

    @FXML
    private void handleSavePreferences(ActionEvent event) {
        StringBuilder categories = new StringBuilder();

        if (prefVegan.isSelected()) categories.append("Vegan,");
        if (prefVegetarian.isSelected()) categories.append("Vegetarian,");
        if (prefKeto.isSelected()) categories.append("Keto,");
        if (prefGlutenFree.isSelected()) categories.append("Gluten-Free,");
        if (prefHalal.isSelected()) categories.append("Halal,");
        if (prefLowCalorie.isSelected()) categories.append("Low-Calorie,");
        if (prefPescatarian.isSelected()) categories.append("Pescatarian,");
        if (prefHighProtein.isSelected()) categories.append("High-Protein,");
        if (prefWeightLoss.isSelected()) categories.append("Weight-Loss,");

        String favoriteCategories = categories.length() > 0
                ? categories.substring(0, categories.length() - 1)
                : "General";

        CustomerPreference pref = new CustomerPreference();
        pref.setCustomerID(currentCustomer.getCustomerID());
        pref.setFavoriteCategories(favoriteCategories);
        pref.setNotificationSettings(notificationsCheckBox.isSelected() ? "Enabled" : "Disabled");
        pref.setDeliveryInstructions(deliveryInstructionsArea.getText().trim());

        boolean success = customerDAO.savePreference(pref);

        if (success) {
            prefMessageLabel.setStyle("-fx-text-fill: #2f6b3f;");
            prefMessageLabel.setText("Preferences saved.");
            AnimationUtil.pulse(prefMessageLabel);
        } else {
            prefMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            prefMessageLabel.setText("Failed to save preferences.");
            AnimationUtil.shake(prefMessageLabel);
        }
    }

    private void setupOrderTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    private void loadOrderHistory() {
        List<OrderHistoryItem> orders = customerDAO.getOrderHistory(currentCustomer.getCustomerID());

        double totalSpent = orders.stream()
                .mapToDouble(OrderHistoryItem::getTotalAmount)
                .sum();

        totalOrdersLabel.setText("0");
        totalSpentLabel.setText(String.format("£%.2f", totalSpent));
        AnimationUtil.countUp(totalOrdersLabel, orders.size());

        orderTable.setItems(FXCollections.observableArrayList(orders));
    }

    @FXML
    private void handleRefreshOrderHistory(ActionEvent event) {
        if (!ordersLoaded) {
            setupOrderTable();
            ordersLoaded = true;
        }
        loadOrderHistory();
    }

    private void setupChefReviews() {
        refreshChefList();
        chefComboBox.setOnAction(event -> handleChefSelected());

        ratingComboBox.setItems(FXCollections.observableArrayList(
                "★ (1 - Poor)",
                "★★ (2 - Fair)",
                "★★★ (3 - Good)",
                "★★★★ (4 - Great)",
                "★★★★★ (5 - Excellent)"
        ));

        reviewChefNameColumn.setCellValueFactory(new PropertyValueFactory<>("chefName"));
        reviewRatingColumn.setCellValueFactory(data ->
                new SimpleStringProperty("★".repeat(data.getValue().getRating()))
        );
        reviewTextColumn.setCellValueFactory(new PropertyValueFactory<>("reviewText"));
        reviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        chefCommentReviewerColumn.setCellValueFactory(new PropertyValueFactory<>("reviewerName"));
        chefCommentRatingColumn.setCellValueFactory(data ->
                new SimpleStringProperty("★".repeat(data.getValue().getRating()))
        );
        chefCommentTextColumn.setCellValueFactory(new PropertyValueFactory<>("reviewText"));
        chefCommentDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        clearChefComments();
    }

    private void handleChefSelected() {
        Chef chef = chefComboBox.getValue();

        if (chef == null) {
            selectedChefNameLabel.setText("-");
            selectedChefSpecialityLabel.setText("-");
            selectedChefRatingLabel.setText("-");
            clearChefComments();
            return;
        }

        selectedChefNameLabel.setText(chef.getChefName());
        selectedChefSpecialityLabel.setText(chef.getSpeciality());

        if (chef.getReviewCount() == 0) {
            selectedChefRatingLabel.setText("New chef — no reviews yet");
        } else {
            int stars = (int) Math.round(chef.getAverageRating());
            selectedChefRatingLabel.setText(
                    "★".repeat(stars) + "☆".repeat(5 - stars)
                            + "  (" + chef.getAverageRating() + ", "
                            + chef.getReviewCount() + " reviews)"
            );
        }
        loadChefComments(chef);
        AnimationUtil.pulse(selectedChefRatingLabel);
    }

    @FXML
    private void handleSubmitReview(ActionEvent event) {
        Chef chef = chefComboBox.getValue();
        int ratingIndex = ratingComboBox.getSelectionModel().getSelectedIndex();
        String reviewText = reviewTextArea.getText().trim();

        if (chef == null) {
            setReviewMessage("Please select a chef.", false);
            return;
        }

        if (ratingIndex < 0) {
            setReviewMessage("Please select a rating.", false);
            return;
        }

        if (reviewText.length() < 10) {
            setReviewMessage("Review must be at least 10 characters.", false);
            return;
        }

        if (reviewText.length() > 500) {
            setReviewMessage("Review must be at most 500 characters.", false);
            return;
        }

        if (ValidationUtil.containsUnsafeCharacters(reviewText)) {
            setReviewMessage("Review contains invalid characters (@#/\\?\"'~`$).", false);
            return;
        }

        if (chefReviewDAO.hasReviewed(currentCustomer.getCustomerID(), chef.getChefID())) {
            setReviewMessage("You have already reviewed this chef.", false);
            return;
        }

        boolean success = chefReviewDAO.addReview(
                currentCustomer.getCustomerID(),
                chef.getChefID(),
                ratingIndex + 1,
                reviewText
        );

        if (success) {
            setReviewMessage("Review submitted successfully.", true);

            reviewTextArea.clear();
            chefComboBox.setValue(null);
            ratingComboBox.setValue(null);

            selectedChefNameLabel.setText("-");
            selectedChefSpecialityLabel.setText("-");
            selectedChefRatingLabel.setText("-");
            clearChefComments();

            refreshChefList();
            loadMyReviews();
        } else {
            setReviewMessage("Failed to submit review. Please try again.", false);
        }
    }

    private void loadMyReviews() {
        reviewTable.setItems(FXCollections.observableArrayList(
                chefReviewDAO.getReviewsByCustomer(currentCustomer.getCustomerID())
        ));
    }

    private void loadChefComments(Chef chef) {
        List<ChefReview> reviews = chefReviewDAO.getReviewsByChef(chef.getChefID());
        chefCommentsTable.setItems(FXCollections.observableArrayList(reviews));
        chefCommentsLabel.setText(reviews.isEmpty()
                ? "No customer comments have been submitted for " + chef.getChefName() + " yet."
                : "Recent customer comments for " + chef.getChefName() + ".");
    }

    private void clearChefComments() {
        if (chefCommentsTable != null) {
            chefCommentsTable.getItems().clear();
        }
        if (chefCommentsLabel != null) {
            chefCommentsLabel.setText("Select a chef to read customer comments.");
        }
    }

    @FXML
    private void handleRefreshChefs(ActionEvent event) {
        refreshChefList();
        setReviewMessage("Chef directory refreshed.", true);
    }

    private void refreshChefList() {
        Chef selectedChef = chefComboBox.getValue();
        List<Chef> chefs = chefReviewDAO.getChefs();
        chefComboBox.setItems(FXCollections.observableArrayList(chefs));

        if (selectedChef != null) {
            chefs.stream()
                    .filter(chef -> chef.getChefID().equals(selectedChef.getChefID()))
                    .findFirst()
                    .ifPresent(chefComboBox::setValue);
            handleChefSelected();
        }
    }

    private void setReviewMessage(String message, boolean success) {
        reviewMessageLabel.setStyle(success ? "-fx-text-fill: #2f6b3f;" : "-fx-text-fill: #b03a2e;");
        reviewMessageLabel.setText(message);
        if (success) {
            AnimationUtil.pulse(reviewMessageLabel);
        } else {
            AnimationUtil.shake(reviewMessageLabel);
        }
    }

    private void startNotificationTimer() {
        notificationTimer = new Timeline(
                new KeyFrame(Duration.minutes(2), event -> showPromoNotification())
        );

        notificationTimer.setCycleCount(Timeline.INDEFINITE);
        notificationTimer.play();
    }

    private void showPromoNotification() {
        CustomerPreference pref = customerDAO.getPreference(currentCustomer.getCustomerID());

        if (pref == null || !"Enabled".equalsIgnoreCase(pref.getNotificationSettings())) {
            return;
        }

        String[] promos = {
                "Today's meal plans are 20% off. Discover our latest healthy options.",
                "New chefs have joined Loop this week. Check out fresh menus.",
                "Weekend special: free delivery on orders over £25.",
                "Your favourite meals are back in stock."
        };

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loop Special Offer");
        alert.setHeaderText("A message from Loop");
        alert.setContentText(promos[random.nextInt(promos.length)]);
        alert.show();
    }

    private void stopNotificationTimer() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
    }

    @FXML
    private void handleBackToFood(ActionEvent event) {
        stopNotificationTimer();
        if (SessionManager.openPersonalizedProducts()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Food Page");
        alert.setHeaderText("The Product page is not open in this session");
        alert.setContentText("Open Customers from the integrated Team Hub to return to the Food page here.");
        alert.showAndWait();
        startNotificationTimer();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        stopNotificationTimer();
        SessionManager.clear();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
    }
}
