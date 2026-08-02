package controllers;

import database.CustomerDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Customer;
import models.OrderHistoryItem;
import utils.AnimationUtil;
import utils.NavigationUtil;
import utils.ValidationUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuperAdminController {

    private static final String ADMIN_PASSWORD = "admin123";

    @FXML private ImageView headerLogo;
    @FXML private PasswordField adminPasswordField;
    @FXML private Label lockMessageLabel;
    @FXML private VBox adminPanel;
    @FXML private Label liveStatusLabel;

    @FXML private TextField searchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> mobileColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, String> statusColumn;

    @FXML private VBox detailPanel;
    @FXML private Label detailTitleLabel;
    @FXML private TextField detailNameField;
    @FXML private TextField detailEmailField;
    @FXML private TextField detailMobileField;
    @FXML private TextField detailIdCardField;
    @FXML private TextField detailAddressField;
    @FXML private ComboBox<String> detailStatusCombo;
    @FXML private ComboBox<String> detailNotificationsCombo;
    @FXML private TextField detailPreferencesField;
    @FXML private TextArea detailInstructionsArea;
    @FXML private Label updateMessageLabel;

    @FXML private Label detailTotalOrdersLabel;
    @FXML private Label detailTotalSpentLabel;
    @FXML private TableView<OrderHistoryItem> detailOrderTable;
    @FXML private TableColumn<OrderHistoryItem, String> detailOrderIdColumn;
    @FXML private TableColumn<OrderHistoryItem, String> detailOrderDateColumn;
    @FXML private TableColumn<OrderHistoryItem, String> detailOrderStatusColumn;
    @FXML private TableColumn<OrderHistoryItem, Double> detailOrderTotalColumn;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private Customer selectedCustomer;
    private Timeline liveRefreshTimer;
    private boolean suppressSelectionListener;
    private String lastLiveSnapshot = "";

    @FXML
    public void initialize() {
        setupTable();
        setupDetailCombos();
        setupOrderTable();
        loadCustomers("");
        hideDetailPanel();

        searchField.setOnAction(event -> loadCustomers(searchField.getText()));

        Platform.runLater(() -> {
            Parent root = adminPasswordField.getScene() != null
                    ? adminPasswordField.getScene().getRoot()
                    : null;
            if (root != null) {
                AnimationUtil.enableButtonEffects(root);
                Node[] cards = root.lookupAll(".info-card").toArray(Node[]::new);
                AnimationUtil.staggerIn(cards);
            }
        });
    }

    private void setupDetailCombos() {
        detailStatusCombo.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        detailNotificationsCombo.setItems(FXCollections.observableArrayList("Enabled", "Disabled"));
    }

    private void setupOrderTable() {
        detailOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        detailOrderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        detailOrderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        detailOrderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (suppressSelectionListener) return;

            selectedCustomer = newValue;
            if (newValue == null) {
                hideDetailPanel();
                return;
            }
            openDetailPanel(newValue, true);
        });
    }

    private void openDetailPanel(Customer customer, boolean animate) {
        detailPanel.setManaged(true);
        detailPanel.setVisible(true);
        populateDetailFields(customer);
        loadCustomerOrders(customer.getCustomerID());
        updateMessageLabel.setText("");

        if (animate) {
            AnimationUtil.popIn(detailPanel);
        }
    }

    private void hideDetailPanel() {
        detailPanel.setManaged(false);
        detailPanel.setVisible(false);
        detailTitleLabel.setText("No customer selected");
        detailOrderTable.getItems().clear();
        detailTotalOrdersLabel.setText("0");
        detailTotalSpentLabel.setText("£0.00");
    }

    private void populateDetailFields(Customer customer) {
        detailTitleLabel.setText("Editing: " + nullSafe(customer.getName())
                + "  (" + nullSafe(customer.getStatus()) + ")");
        detailNameField.setText(nullSafe(customer.getName()));
        detailEmailField.setText(nullSafe(customer.getEmail()));
        detailMobileField.setText(nullSafe(customer.getMobile()));
        detailIdCardField.setText(nullSafe(customer.getIdCardNo()));
        detailAddressField.setText(nullSafe(customer.getDeliveryAddress()));
        detailStatusCombo.setValue(
                "Inactive".equalsIgnoreCase(customer.getStatus()) ? "Inactive" : "Active");
        detailNotificationsCombo.setValue(
                "Disabled".equalsIgnoreCase(customer.getNotificationSettings()) ? "Disabled" : "Enabled");
        detailPreferencesField.setText(nullSafe(customer.getFavoriteCategories()));
        detailInstructionsArea.setText(nullSafe(customer.getDeliveryInstructions()));
    }

    private void loadCustomerOrders(String customerID) {
        List<OrderHistoryItem> orders = customerDAO.getOrderHistory(customerID);
        double totalSpent = orders.stream().mapToDouble(OrderHistoryItem::getTotalAmount).sum();

        detailOrderTable.setItems(FXCollections.observableArrayList(orders));
        detailTotalOrdersLabel.setText(String.valueOf(orders.size()));
        detailTotalSpentLabel.setText(String.format("£%.2f", totalSpent));
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String liveSnapshot(Customer customer) {
        if (customer == null) return "";
        return nullSafe(customer.getName()) + "|"
                + nullSafe(customer.getEmail()) + "|"
                + nullSafe(customer.getMobile()) + "|"
                + nullSafe(customer.getDeliveryAddress()) + "|"
                + nullSafe(customer.getIdCardNo()) + "|"
                + nullSafe(customer.getStatus()) + "|"
                + nullSafe(customer.getFavoriteCategories()) + "|"
                + nullSafe(customer.getNotificationSettings()) + "|"
                + nullSafe(customer.getDeliveryInstructions());
    }

    private void loadCustomers(String searchTerm) {
        String selectedId = selectedCustomer != null ? selectedCustomer.getCustomerID() : null;
        List<Customer> customers = customerDAO.getAllCustomers(searchTerm);

        suppressSelectionListener = true;
        customerTable.setItems(FXCollections.observableArrayList(customers));

        if (selectedId != null) {
            Customer matched = null;
            for (Customer customer : customers) {
                if (selectedId.equals(customer.getCustomerID())) {
                    matched = customer;
                    break;
                }
            }

            if (matched != null) {
                customerTable.getSelectionModel().select(matched);
                selectedCustomer = matched;

                String snapshot = liveSnapshot(matched);
                if (detailPanel.isVisible()) {
                    populateDetailFields(matched);
                    loadCustomerOrders(matched.getCustomerID());
                }
                lastLiveSnapshot = snapshot;
            } else {
                selectedCustomer = null;
                hideDetailPanel();
                lastLiveSnapshot = "";
            }
        }

        suppressSelectionListener = false;
    }

    private void startLiveRefresh() {
        stopLiveRefresh();

        liveRefreshTimer = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> {
                    if (!adminPanel.isDisable()) {
                        loadCustomers(searchField.getText());
                        liveStatusLabel.setText("● LIVE  "
                                + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                })
        );
        liveRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        liveRefreshTimer.play();

        liveStatusLabel.setText("● LIVE tracking on");
        AnimationUtil.startGlowPulse(liveStatusLabel);
    }

    private void stopLiveRefresh() {
        if (liveRefreshTimer != null) {
            liveRefreshTimer.stop();
            liveRefreshTimer = null;
        }
        if (liveStatusLabel != null) {
            liveStatusLabel.setText("");
            liveStatusLabel.setOpacity(1);
        }
    }

    @FXML
    private void handleUnlock(ActionEvent event) {
        if (ADMIN_PASSWORD.equals(adminPasswordField.getText())) {
            adminPanel.setDisable(false);
            AnimationUtil.popIn(adminPanel);
            lockMessageLabel.setStyle("-fx-text-fill: #2f6b3f;");
            lockMessageLabel.setText("Access granted. Click a customer row to open the detail box.");
            AnimationUtil.pulse(lockMessageLabel);
            loadCustomers(searchField.getText());
            startLiveRefresh();
        } else {
            lockMessageLabel.setStyle("-fx-text-fill: #b03a2e;");
            lockMessageLabel.setText("Incorrect admin password.");
            AnimationUtil.shake(lockMessageLabel);
            AnimationUtil.shake(adminPasswordField);
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        loadCustomers(searchField.getText());
        AnimationUtil.fadeInContent(customerTable);
    }

    @FXML
    private void handleCloseDetail(ActionEvent event) {
        customerTable.getSelectionModel().clearSelection();
        selectedCustomer = null;
        lastLiveSnapshot = "";
        hideDetailPanel();
    }

    @FXML
    private void handleToggleStatus(ActionEvent event) {
        if (selectedCustomer == null) {
            setUpdateMessage("Select a customer first.", false);
            return;
        }

        boolean success = customerDAO.toggleStatus(
                selectedCustomer.getCustomerID(),
                selectedCustomer.getStatus()
        );

        if (success) {
            setUpdateMessage("Customer status toggled.", true);
            loadCustomers(searchField.getText());
        } else {
            setUpdateMessage("Failed to toggle status.", false);
        }
    }

    @FXML
    private void handleSaveAll(ActionEvent event) {
        if (adminPanel.isDisable()) {
            setUpdateMessage("Unlock Super Admin access first.", false);
            return;
        }
        if (selectedCustomer == null) {
            setUpdateMessage("Select a customer first.", false);
            return;
        }

        String name = detailNameField.getText().trim();
        String email = detailEmailField.getText().trim();
        String mobile = detailMobileField.getText().trim();
        String idCard = detailIdCardField.getText().trim();
        String address = detailAddressField.getText().trim();
        String status = detailStatusCombo.getValue();
        String notifications = detailNotificationsCombo.getValue();
        String preferences = detailPreferencesField.getText().trim();
        String instructions = detailInstructionsArea.getText().trim();

        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isNotEmpty(address)) {
            setUpdateMessage("Name and address are required.", false);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            setUpdateMessage("Enter a valid email address.", false);
            return;
        }
        if (!ValidationUtil.isValidMobile(mobile)) {
            setUpdateMessage("Mobile must be 10-15 digits.", false);
            return;
        }
        if (!ValidationUtil.isValidIdCard(idCard)) {
            setUpdateMessage("ID card must be 4-30 characters (letters, numbers, dash or space).", false);
            return;
        }
        if (customerDAO.emailExistsForOtherPerson(email, selectedCustomer.getPersonID())) {
            setUpdateMessage("This email already belongs to another customer.", false);
            return;
        }
        if (customerDAO.idCardExistsForOtherCustomer(idCard, selectedCustomer.getCustomerID())) {
            setUpdateMessage("This ID card already belongs to another customer.", false);
            return;
        }

        Customer updated = new Customer();
        updated.setCustomerID(selectedCustomer.getCustomerID());
        updated.setPersonID(selectedCustomer.getPersonID());
        updated.setName(name);
        updated.setEmail(email);
        updated.setMobile(mobile);
        updated.setIdCardNo(idCard);
        updated.setDeliveryAddress(address);
        updated.setStatus(status == null ? "Active" : status);

        boolean success = customerDAO.superAdminUpdateCustomer(
                updated,
                preferences.isBlank() ? "General" : preferences,
                notifications == null ? "Enabled" : notifications,
                instructions
        );

        if (success) {
            setUpdateMessage("All customer details saved successfully.", true);
            lastLiveSnapshot = "";
            loadCustomers(searchField.getText());
        } else {
            setUpdateMessage("Update failed. Please try again.", false);
        }
    }

    private void setUpdateMessage(String message, boolean success) {
        updateMessageLabel.setStyle(success ? "-fx-text-fill: #2f6b3f;" : "-fx-text-fill: #b03a2e;");
        updateMessageLabel.setText(message);
        if (success) {
            AnimationUtil.pulse(updateMessageLabel);
        } else {
            AnimationUtil.shake(updateMessageLabel);
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        stopLiveRefresh();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.switchScene(stage, "/view/Login.fxml", "/styles/app.css");
    }
}
