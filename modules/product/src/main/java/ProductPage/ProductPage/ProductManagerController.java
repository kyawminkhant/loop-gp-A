package ProductPage.ProductPage;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProductManagerController {

    private static final double FILTER_POPUP_X_OFFSET = 0;
    private static final double FILTER_POPUP_Y_OFFSET = 10;

    @FXML private TextField searchField;
    @FXML private VBox productRows;
    @FXML private VBox managerFilterPopup;
    @FXML private VBox managerActionPopup;
    @FXML private Button managerFilterButton;
    @FXML private Label totalProductsLabel;
    @FXML private Label statusLabel;
    @FXML private ToggleButton managerNewestToggle;
    @FXML private ToggleButton managerNameToggle;
    @FXML private ToggleButton managerCheapestToggle;
    @FXML private ToggleButton managerHighestToggle;
    @FXML private ToggleButton managerStatusOnToggle;
    @FXML private ToggleButton managerStatusOffToggle;
    @FXML private ToggleButton managerVegetarianToggle;
    @FXML private ToggleButton managerVeganToggle;
    @FXML private ToggleButton managerHalalToggle;
    @FXML private ToggleButton managerGlutenFreeToggle;
    @FXML private ToggleButton managerPescatarianToggle;
    @FXML private ToggleButton managerWeightLossToggle;
    @FXML private ToggleButton managerHighProteinToggle;
    @FXML private ToggleButton managerBalancedMealsToggle;
    @FXML private ToggleButton managerAsianToggle;
    @FXML private ToggleButton managerSoutheastAsianToggle;
    @FXML private ToggleButton managerSouthAsianToggle;
    @FXML private ToggleButton managerMiddleEasternToggle;
    @FXML private ToggleButton managerWesternToggle;
    @FXML private ToggleButton managerMediterraneanToggle;
    @FXML private TextField managerMinPriceField;
    @FXML private TextField managerMaxPriceField;
    @FXML private Label managerPriceErrorLabel;
    @FXML private Button managerBackButton;

    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
    private final DateTimeFormatter databaseDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private List<SellerProductRepository.ProductSummary> allProducts = new ArrayList<>();
    private String statusFilter = "ALL";
    private String sortMode = "NEWEST";
    private String appliedSearchQuery = "";
    private String dietaryFilter;
    private String healthGoalFilter;
    private String cuisineFilter;
    private Double minPriceFilter;
    private Double maxPriceFilter;
    private SellerProductRepository.ProductSummary selectedActionProduct;
    private ToggleButton selectedActionOwner;
    private int selectedActionProductId = -1;
    private String selectedActionProductName = "";

    @FXML
    private void initialize() {
        managerBackButton.setText(ProductManagementContext.hasAdminReturn()
                ? "<  Back To Admin"
                : "<  Back To Hub");
        loadProducts();
    }

    @FXML
    private void backToHub() {
        if (ProductManagementContext.returnToAdmin()) {
            return;
        }
        try {
            Stage stage = (Stage) productRows.getScene().getWindow();
            App.showHub(stage);
        } catch (IOException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not return to the Team Hub.");
        }
    }

    @FXML
    private void openAddProduct() {
        SellerPageController.prepareAddProduct();
        setRoot("Seller Page");
    }

    @FXML
    private void runSearch() {
        appliedSearchQuery = clean(searchField.getText());
        renderProducts();
        searchField.requestFocus();
    }

    @FXML
    private void toggleFilterPopup(ActionEvent event) {
        hideActionPopup();
        boolean show = managerFilterPopup == null || !managerFilterPopup.isVisible();
        if (managerFilterPopup != null) {
            managerFilterPopup.setVisible(show);
            managerFilterPopup.setManaged(false);
            if (show) {
                managerFilterPopup.applyCss();
                managerFilterPopup.autosize();
                if (managerFilterButton != null) {
                    movePopupUnderOwner(managerFilterPopup, managerFilterButton, FILTER_POPUP_X_OFFSET, FILTER_POPUP_Y_OFFSET);
                }
                managerFilterPopup.toFront();
            }
        }
    }

    @FXML
    private void applyManagerFilters() {
        Double minPrice;
        Double maxPrice;
        try {
            minPrice = parseOptionalMoney(managerMinPriceField == null ? "" : managerMinPriceField.getText());
            maxPrice = parseOptionalMoney(managerMaxPriceField == null ? "" : managerMaxPriceField.getText());
        } catch (NumberFormatException exception) {
            showPriceError("Enter valid numeric prices.");
            return;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            showPriceError("Minimum price must be less than maximum price.");
            return;
        }

        clearPriceError();
        sortMode = selectedSortMode();
        statusFilter = selectedStatusFilter();
        dietaryFilter = selectedDietaryFilter();
        healthGoalFilter = selectedHealthGoalFilter();
        cuisineFilter = selectedCuisineFilter();
        minPriceFilter = minPrice;
        maxPriceFilter = maxPrice;
        closeFilterPopup();
        renderProducts();
        statusLabel.setText(filterStatusMessage());
    }

    @FXML
    private void clearManagerFilters() {
        selectToggle(managerNewestToggle);
        clearToggle(managerStatusOnToggle);
        clearToggle(managerStatusOffToggle);
        clearToggle(managerVegetarianToggle);
        clearToggle(managerVeganToggle);
        clearToggle(managerHalalToggle);
        clearToggle(managerGlutenFreeToggle);
        clearToggle(managerPescatarianToggle);
        clearToggle(managerWeightLossToggle);
        clearToggle(managerHighProteinToggle);
        clearToggle(managerBalancedMealsToggle);
        clearToggle(managerAsianToggle);
        clearToggle(managerSoutheastAsianToggle);
        clearToggle(managerSouthAsianToggle);
        clearToggle(managerMiddleEasternToggle);
        clearToggle(managerWesternToggle);
        clearToggle(managerMediterraneanToggle);
        if (managerMinPriceField != null) {
            managerMinPriceField.clear();
        }
        if (managerMaxPriceField != null) {
            managerMaxPriceField.clear();
        }
        clearPriceError();
        sortMode = "NEWEST";
        statusFilter = "ALL";
        dietaryFilter = null;
        healthGoalFilter = null;
        cuisineFilter = null;
        minPriceFilter = null;
        maxPriceFilter = null;
        closeFilterPopup();
        renderProducts();
        statusLabel.setText("");
    }

    private void loadProducts() {
        try {
            allProducts = SellerProductRepository.loadProductSummaries();
            renderProducts();
            long inventoryUnavailable = allProducts.stream()
                    .filter(product -> product.manuallyActive && !product.inventoryAvailable)
                    .count();
            statusLabel.setText(inventoryUnavailable == 0
                    ? ""
                    : inventoryUnavailable + " product(s) automatically inactive due to missing stock.");
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not load product_Products from the database.");
        }
    }

    private void renderProducts() {
        productRows.getChildren().clear();

        String query = appliedSearchQuery.toLowerCase();
        List<SellerProductRepository.ProductSummary> visible = allProducts.stream()
                .filter(product -> query.isBlank()
                        || product.productName.toLowerCase().contains(query)
                        || product.category.toLowerCase().contains(query))
                .filter(product -> "ALL".equals(statusFilter)
                        || ("ACTIVE".equals(statusFilter) && product.active)
                        || ("INACTIVE".equals(statusFilter) && !product.active))
                .filter(product -> matchesFilter(product.dietary, dietaryFilter))
                .filter(product -> matchesFilter(product.healthGoal, healthGoalFilter))
                .filter(product -> matchesFilter(product.cuisine, cuisineFilter))
                .filter(product -> minPriceFilter == null || product.price >= minPriceFilter)
                .filter(product -> maxPriceFilter == null || product.price <= maxPriceFilter)
                .collect(Collectors.toList());

        sortProducts(visible);

        totalProductsLabel.setText("Total Products: " + visible.size());

        if (visible.isEmpty()) {
            Label empty = new Label("No products found.");
            empty.getStyleClass().add("manager-empty-label");
            productRows.getChildren().add(empty);
            return;
        }

        for (SellerProductRepository.ProductSummary product : visible) {
            productRows.getChildren().add(createProductRow(product));
        }
    }

    private HBox createProductRow(SellerProductRepository.ProductSummary product) {
        HBox row = new HBox(16);
        row.getStyleClass().add("manager-product-row");
        row.getStyleClass().add(product.active ? "manager-product-row-active" : "manager-product-row-inactive");
        row.setMaxWidth(Double.MAX_VALUE);

        String displayName = product.productName;
        if (!product.inventoryAvailable) {
            displayName += "\nMissing stock: " + String.join(", ", product.missingIngredients);
        }
        Label name = new Label(displayName);
        name.getStyleClass().add("manager-col-product");
        if (!product.inventoryAvailable) {
            name.getStyleClass().add("manager-missing-stock");
        }
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);

        Label category = new Label(product.category);
        category.getStyleClass().addAll("manager-col-category", "manager-category-pill");
        if (!product.active) {
            category.getStyleClass().add("manager-category-pill-inactive");
        }

        Label price = new Label(moneyFormat.format(product.price));
        price.getStyleClass().add("manager-col-money");

        Label cost = new Label(moneyFormat.format(product.cost));
        cost.getStyleClass().add("manager-col-money");

        Label updated = new Label(formatDate(product.updatedDate));
        updated.getStyleClass().add("manager-col-date");
        updated.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(updated, javafx.scene.layout.Priority.ALWAYS);

        ImageView optionIcon = new ImageView(new Image(App.class.getResourceAsStream("images/option.png")));
        optionIcon.setFitWidth(26);
        optionIcon.setFitHeight(26);
        optionIcon.setPreserveRatio(true);

        ToggleButton action = new ToggleButton();
        action.setGraphic(optionIcon);
        action.getStyleClass().add("manager-action-button");
        action.setOnAction(event -> showProductActionPopup(action, product));

        HBox actionBox = new HBox(action);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.getStyleClass().add("manager-col-action");

        row.getChildren().addAll(name, category, price, cost, updated, actionBox);
        return row;
    }

    private void showProductActionPopup(ToggleButton owner, SellerProductRepository.ProductSummary product) {
        closeFilterPopup();
        if (selectedActionOwner != null && selectedActionOwner != owner) {
            selectedActionOwner.setSelected(false);
        }

        boolean show = owner.isSelected();
        if (!show) {
            hideActionPopup();
            return;
        }

        selectedActionProduct = product;
        selectedActionOwner = owner;
        selectedActionProductId = product.id;
        selectedActionProductName = product.productName;
        if (managerActionPopup == null) {
            return;
        }

        managerActionPopup.setVisible(true);
        managerActionPopup.setManaged(false);
        managerActionPopup.applyCss();
        managerActionPopup.autosize();
        movePopupCenteredUnderOwner(managerActionPopup, owner, 8);
        managerActionPopup.toFront();
    }

    @FXML
    private void toggleSelectedProductStatus() {
        if (selectedActionProductId < 0) {
            return;
        }
        int productId = selectedActionProductId;
        hideActionPopup();
        toggleProductStatus(productId);
    }

    @FXML
    private void modifySelectedProduct() {
        if (selectedActionProductId < 0) {
            return;
        }
        int productId = selectedActionProductId;
        hideActionPopup();
        openEditProduct(productId);
    }

    @FXML
    private void deleteSelectedProduct() {
        if (selectedActionProductId < 0) {
            return;
        }
        int productId = selectedActionProductId;
        String productName = selectedActionProductName;
        hideActionPopup();
        deleteProduct(productId, productName);
    }

    private void toggleProductStatus(int productId) {
        try {
            SellerProductRepository.toggleProductStatus(productId);
            loadProducts();
            statusLabel.setText("Product status updated in the database.");
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not update this product status.");
        }
    }

    private void deleteProduct(int productId, String productName) {
        try {
            SellerProductRepository.deleteProduct(productId);
            loadProducts();
            statusLabel.setText(productName + " deleted.");
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not delete this product.");
        }
    }

    private String formatDate(String value) {
        String clean = clean(value);
        if (clean.isBlank()) {
            return "";
        }

        try {
            return LocalDateTime.parse(clean, databaseDate).format(displayDate);
        } catch (DateTimeParseException exception) {
            return clean.length() >= 10 ? clean.substring(0, 10) : clean;
        }
    }

    private void setRoot(String fxmlName) {
        try {
            App.setRoot(fxmlName);
        } catch (IOException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not open " + fxmlName + ".fxml.");
        }
    }

    private void openEditProduct(int productId) {
        SellerPageController.prepareEditProduct(productId);
        setRoot("Seller Page");
    }

    private String selectedSortMode() {
        if (isSelected(managerNameToggle)) {
            return "NAME";
        }
        if (isSelected(managerCheapestToggle)) {
            return "CHEAPEST";
        }
        if (isSelected(managerHighestToggle)) {
            return "HIGHEST";
        }
        return "NEWEST";
    }

    private String selectedStatusFilter() {
        if (isSelected(managerStatusOnToggle)) {
            return "ACTIVE";
        }
        if (isSelected(managerStatusOffToggle)) {
            return "INACTIVE";
        }
        return "ALL";
    }

    private String selectedDietaryFilter() {
        if (isSelected(managerVegetarianToggle)) {
            return "Vegetarian";
        }
        if (isSelected(managerVeganToggle)) {
            return "Vegan";
        }
        if (isSelected(managerHalalToggle)) {
            return "Halal";
        }
        if (isSelected(managerGlutenFreeToggle)) {
            return "Gluten-Free";
        }
        if (isSelected(managerPescatarianToggle)) {
            return "Pescatarian";
        }
        return null;
    }

    private String selectedHealthGoalFilter() {
        if (isSelected(managerWeightLossToggle)) {
            return "Weight Loss";
        }
        if (isSelected(managerHighProteinToggle)) {
            return "High Protein";
        }
        if (isSelected(managerBalancedMealsToggle)) {
            return "Balanced Meals";
        }
        return null;
    }

    private String selectedCuisineFilter() {
        if (isSelected(managerAsianToggle)) {
            return "Asian";
        }
        if (isSelected(managerSoutheastAsianToggle)) {
            return "Southeast Asian";
        }
        if (isSelected(managerSouthAsianToggle)) {
            return "South Asian";
        }
        if (isSelected(managerMiddleEasternToggle)) {
            return "Middle Eastern";
        }
        if (isSelected(managerWesternToggle)) {
            return "Western";
        }
        if (isSelected(managerMediterraneanToggle)) {
            return "Mediterranean";
        }
        return null;
    }

    private String filterStatusMessage() {
        String sortText;
        switch (sortMode) {
            case "NAME":
                sortText = "product name";
                break;
            case "CHEAPEST":
                sortText = "lowest price";
                break;
            case "HIGHEST":
                sortText = "highest price";
                break;
            default:
                sortText = "newest updated";
                break;
        }

        if ("ACTIVE".equals(statusFilter)) {
            return "Showing active products sorted by " + sortText + ".";
        }
        if ("INACTIVE".equals(statusFilter)) {
            return "Showing inactive products sorted by " + sortText + ".";
        }
        return "Showing all products sorted by " + sortText + ".";
    }

    private void sortProducts(List<SellerProductRepository.ProductSummary> products) {
        switch (sortMode) {
            case "NAME":
                products.sort((left, right) -> left.productName.compareToIgnoreCase(right.productName));
                break;
            case "CHEAPEST":
                products.sort((left, right) -> Double.compare(left.price, right.price));
                break;
            case "HIGHEST":
                products.sort((left, right) -> Double.compare(right.price, left.price));
                break;
            default:
                products.sort((left, right) -> right.updatedDate.compareToIgnoreCase(left.updatedDate));
                break;
        }
    }

    private void closeFilterPopup() {
        if (managerFilterPopup != null) {
            managerFilterPopup.setVisible(false);
            managerFilterPopup.setManaged(false);
        }
    }

    private void hideActionPopup() {
        if (selectedActionOwner != null) {
            selectedActionOwner.setSelected(false);
        }
        selectedActionProduct = null;
        selectedActionOwner = null;
        selectedActionProductId = -1;
        selectedActionProductName = "";
        if (managerActionPopup != null) {
            managerActionPopup.setVisible(false);
            managerActionPopup.setManaged(false);
        }
    }

    private void movePopupUnderOwner(VBox popup, Node owner, double xOffset, double yOffset) {
        Bounds ownerBounds = owner.localToScene(owner.getBoundsInLocal());
        Point2D ownerBottomLeft = popup.getParent().sceneToLocal(ownerBounds.getMinX(), ownerBounds.getMaxY());

        double popupWidth = popup.prefWidth(-1) > 0 ? popup.prefWidth(-1) : popup.getBoundsInLocal().getWidth();
        double popupHeight = popup.prefHeight(-1) > 0 ? popup.prefHeight(-1) : popup.getBoundsInLocal().getHeight();
        double parentWidth = popup.getParent().getLayoutBounds().getWidth();
        double parentHeight = popup.getParent().getLayoutBounds().getHeight();

        double x = ownerBottomLeft.getX() + xOffset;
        double y = ownerBottomLeft.getY() + yOffset;

        x = clamp(x, 0, Math.max(0, parentWidth - popupWidth));
        y = clamp(y, 0, Math.max(0, parentHeight - popupHeight));

        popup.setTranslateX(0);
        popup.setTranslateY(0);
        popup.relocate(x, y);
    }

    private void movePopupCenteredUnderOwner(VBox popup, Node owner, double yOffset) {
        Bounds ownerBounds = owner.localToScene(owner.getBoundsInLocal());
        Point2D ownerTopLeft = popup.getParent().sceneToLocal(ownerBounds.getMinX(), ownerBounds.getMinY());
        Point2D ownerBottomRight = popup.getParent().sceneToLocal(ownerBounds.getMaxX(), ownerBounds.getMaxY());

        double ownerCenterX = (ownerTopLeft.getX() + ownerBottomRight.getX()) / 2;
        double popupWidth = popup.prefWidth(-1) > 0 ? popup.prefWidth(-1) : popup.getBoundsInLocal().getWidth();
        double popupHeight = popup.prefHeight(-1) > 0 ? popup.prefHeight(-1) : popup.getBoundsInLocal().getHeight();
        double parentWidth = popup.getParent().getLayoutBounds().getWidth();
        double parentHeight = popup.getParent().getLayoutBounds().getHeight();

        double x = ownerCenterX - (popupWidth / 2);
        double y = ownerBottomRight.getY() + yOffset;

        x = clamp(x, 0, Math.max(0, parentWidth - popupWidth));
        y = clamp(y, 0, Math.max(0, parentHeight - popupHeight));

        popup.setTranslateX(0);
        popup.setTranslateY(0);
        popup.relocate(x, y);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private boolean isSelected(ToggleButton toggle) {
        return toggle != null && toggle.isSelected();
    }

    private void selectToggle(ToggleButton toggle) {
        if (toggle != null) {
            toggle.setSelected(true);
        }
    }

    private void clearToggle(ToggleButton toggle) {
        if (toggle != null) {
            toggle.setSelected(false);
        }
    }

    private boolean matchesFilter(String actualValue, String selectedValue) {
        return selectedValue == null || selectedValue.equalsIgnoreCase(clean(actualValue));
    }

    private Double parseOptionalMoney(String value) {
        String cleaned = clean(value).replaceAll("[£Â]", "");
        if (cleaned.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException exception) {
            throw exception;
        }
    }

    private void showPriceError(String message) {
        if (managerPriceErrorLabel != null) {
            managerPriceErrorLabel.setText(message);
            managerPriceErrorLabel.setVisible(true);
            managerPriceErrorLabel.setManaged(true);
        }
    }

    private void clearPriceError() {
        if (managerPriceErrorLabel != null) {
            managerPriceErrorLabel.setText("");
            managerPriceErrorLabel.setVisible(false);
            managerPriceErrorLabel.setManaged(false);
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
