package ProductPage.ProductPage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ViewFoodController {

    @FXML private ImageView foodImageView;
    @FXML private Label imageCounterLabel;
    @FXML private Label foodNameLabel;
    @FXML private Label priceLabel;
    @FXML private Label ratingCountLabel;
    @FXML private HBox ratingStarsBox;
    @FXML private Label dietaryLabel;
    @FXML private Label goalLabel;
    @FXML private Label cuisineLabel;
    @FXML private StackPane countryFlagFrame;
    @FXML private ImageView countryFlagImage;
    @FXML private Label countryLabel;
    @FXML private VBox suggestionList;
    @FXML private Label quantityLabel;
    @FXML private Button subscriptionButton;
    @FXML private Button addToCartButton;
    @FXML private Label topCaloriesLabel;
    @FXML private HBox topSpiceBox;
    @FXML private GridPane nutritionTable;
    @FXML private Label tabInfoLabel;
    @FXML private VBox overviewContent;
    @FXML private VBox ingredientsContent;
    @FXML private VBox chefContent;
    @FXML private ImageView chefImageView;
    @FXML private ImageView chefInspiredIcon;
    @FXML private ImageView chefHealthIcon;
    @FXML private ImageView chefExperienceIcon;
    @FXML private Label chefNameLabel;
    @FXML private Label chefDescriptionLabel;
    @FXML private Label chefTag1Label;
    @FXML private Label chefTag2Label;
    @FXML private Label chefTag3Label;
    @FXML private Label chefEmailLabel;
    @FXML private Label chefTelLabel;
    @FXML private Button chefRatingReviewButton;
    @FXML private HBox chefRatingStarsBox;
    @FXML private VBox suggestionPanel;
    @FXML private VBox baseChoiceBox;
    @FXML private VBox proteinChoiceBox;
    @FXML private HBox spiceChoiceBox;
    @FXML private VBox addOnColumnLeft;
    @FXML private VBox addOnColumnRight;
    @FXML private VBox portionChoiceBox;
    @FXML private Button overviewTabButton;
    @FXML private Button ingredientsTabButton;
    @FXML private Button chefTabButton;
    @FXML private Button reviewsTabButton;
    @FXML private Label allergenAlertLabel;

    private final ArrayList<String> images = new ArrayList<>();
    private int imageIndex;
    private int quantity = 1;
    private boolean subscriptionSelected;
    private FoodBarData currentFood;
    private final ToggleGroup spiceToggleGroup = new ToggleGroup();
    private final ToggleGroup portionToggleGroup = new ToggleGroup();

    @FXML
    private void initialize() {
        if (foodImageView != null) {
            Rectangle imageClip = new Rectangle(320, 320);
            imageClip.setArcWidth(28);
            imageClip.setArcHeight(28);
            foodImageView.setClip(imageClip);
        }

        if (chefImageView != null) {
            Rectangle chefClip = new Rectangle(300, 430);
            chefClip.setArcWidth(42);
            chefClip.setArcHeight(42);
            chefImageView.setClip(chefClip);
            setDummyChefImage();
        }

        configureChefFeatureIcons(
            "Inspired by Asian Street Food",
            "Health-Driven Cooking",
            "18+ Years of Culinary Experience"
        );

        if (countryFlagFrame != null) {
            Circle flagClip = new Circle(43, 43, 43);
            countryFlagFrame.setClip(flagClip);
        }

        if (spiceChoiceBox != null) {
            configureToggleGroup(spiceChoiceBox, spiceToggleGroup);
        }

        if (portionChoiceBox != null) {
            configureToggleGroup(portionChoiceBox, portionToggleGroup);
            configurePortionPricing();
        }
    }

    @FXML
    private void goBackToMenu() throws IOException {
        Parent menuRoot = FXMLLoader.load(
            getClass().getResource("Main Page.fxml")
        );
        foodImageView.getScene().setRoot(menuRoot);
    }

    public void setFood(FoodBarData food) {
        showFood(food);
    }

    @FXML
    private void showPreviousImage() {
        if (images.isEmpty()) {
            return;
        }

        imageIndex = (imageIndex - 1 + images.size()) % images.size();
        showCurrentImage();
    }

    @FXML
    private void showNextImage() {
        if (images.isEmpty()) {
            return;
        }

        imageIndex = (imageIndex + 1) % images.size();
        showCurrentImage();
    }

    private void showFood(FoodBarData food) {
        currentFood = food;
        quantity = 1;
        subscriptionSelected = false;

        foodNameLabel.setText(food.getProductName());
        ratingCountLabel.setText(
            String.format(
                "%.1f (%s)",
                food.getAverageRating(),
                NumberFormat.getIntegerInstance().format(food.getRatingCount())
            )
        );
        dietaryLabel.setText(food.getDietary());
        goalLabel.setText(food.getHealthGoal());
        cuisineLabel.setText(food.getCuisine());
        setTagIcon(dietaryLabel, food.getDietary(), "dietary", food.getDietaryIconLocation());
        setTagIcon(goalLabel, food.getHealthGoal(), "healthy-goals", food.getGoalIconLocation());
        setTagIcon(cuisineLabel, food.getCuisine(), "cuisine", food.getCuisineIconLocation());
        countryLabel.setText(food.getCountry());
        setCountryFlag(food.getCountry());
        tabInfoLabel.setText(food.getExtendedDescription());
        updateAllergenAlert(food);
        populateNutritionTable(food);
        populateChefInfo(food);
        topCaloriesLabel.setText(String.format("%.0f kcal", food.getTotalCalories()));
        setSpiceLevel(food.getSpiceLevel());
        selectSpiceChoice(food.getSpiceLevel());
        selectToggleByText(portionChoiceBox, "Regular");
        populateIngredientChoices(food);
        showOverviewContent();

        setRating(food.getAverageRating());

        images.clear();
        images.addAll(food.getProductImageLocations());
        imageIndex = 0;
        showCurrentImage();
        showSuggestions(food);

        updateCartControls();
    }

    @FXML
    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            updateCartControls();
        }
    }

    @FXML
    private void increaseQuantity() {
        quantity++;
        updateCartControls();
    }

    @FXML
    private void toggleSubscription() {
        subscriptionSelected = !subscriptionSelected;
        updateCartControls();
    }

    @FXML
    private void addToCart() throws IOException {
        if (currentFood == null) {
            return;
        }

        CartStore.addItem(
            currentFood.getProductId(),
            currentItemPrice(),
            quantity,
            subscriptionSelected,
            currentCartDetailSummary()
        );

        double[][] cartOutput = CartStore.getProductPriceArray();
        for (double[] row : cartOutput) {
            System.out.println(
                "ProductID: " + (int) row[0] + ", Price: \u00A3" + row[1]
            );
        }

        String[][] productDetailOutput = CartStore.getProductDetailArray();
        for (String[] row : productDetailOutput) {
            System.out.println(
                "ProductID: " + row[0] + ", Details: " + row[1]
            );
        }

        goBackToMenu();
    }

    @FXML
    private void showOverviewTab() {
        setActiveTab(overviewTabButton);
        showOverviewContent();
        tabInfoLabel.setText(
            currentFood == null
                ? "Food overview will appear here."
                : currentFood.getExtendedDescription()
        );
    }

    @FXML
    private void showIngredientsTab() {
        setActiveTab(ingredientsTabButton);
        showIngredientContent();
    }

    @FXML
    private void showChefTab() {
        setActiveTab(chefTabButton);
        showChefContent();
    }

    @FXML
    private void showReviewsTab() {
        setActiveTab(reviewsTabButton);
        showOverviewContent();
        setSuggestionsVisible(false);
        tabInfoLabel.setText(
            currentFood == null
                ? "Reviews will appear here."
                : String.format(
                    "%.1f rating from %s people.",
                    currentFood.getAverageRating(),
                    NumberFormat.getIntegerInstance().format(
                        currentFood.getRatingCount()
                    )
                )
        );
    }

    private void updateCartControls() {
        quantityLabel.setText(String.valueOf(quantity));
        subscriptionButton.setText(
            subscriptionSelected ? "Weekly On" : "Weekly"
        );
        subscriptionButton.getStyleClass().remove("subscription-selected");
        if (subscriptionSelected) {
            subscriptionButton.getStyleClass().add("subscription-selected");
        }
        double itemPrice = currentItemPrice();
        priceLabel.setText(String.format("\u00A3%,.2f", itemPrice));
        addToCartButton.setText("Add to Cart");
    }

    private void showOverviewContent() {
        overviewContent.setVisible(true);
        overviewContent.setManaged(true);
        ingredientsContent.setVisible(false);
        ingredientsContent.setManaged(false);
        chefContent.setVisible(false);
        chefContent.setManaged(false);
        setSuggestionsVisible(true);
    }

    private void showIngredientContent() {
        overviewContent.setVisible(false);
        overviewContent.setManaged(false);
        ingredientsContent.setVisible(true);
        ingredientsContent.setManaged(true);
        chefContent.setVisible(false);
        chefContent.setManaged(false);
        setSuggestionsVisible(false);
    }

    private void showChefContent() {
        overviewContent.setVisible(false);
        overviewContent.setManaged(false);
        ingredientsContent.setVisible(false);
        ingredientsContent.setManaged(false);
        chefContent.setVisible(true);
        chefContent.setManaged(true);
        setSuggestionsVisible(false);
    }

    private void setSuggestionsVisible(boolean visible) {
        if (suggestionPanel == null) {
            return;
        }

        suggestionPanel.setVisible(visible);
        suggestionPanel.setManaged(visible);
    }

    private void setDummyChefImage() {
        String imageUrl = resolveImageUrl("/ProductPage/ProductPage/images/user.png");
        if (imageUrl == null) {
            chefImageView.setImage(null);
            return;
        }

        Image chefImage = new Image(imageUrl, 0, 0, true, true, false);
        chefImageView.setViewport(centerCropViewport(chefImage, 300, 430));
        chefImageView.setPreserveRatio(false);
        chefImageView.setFitWidth(300);
        chefImageView.setFitHeight(430);
        chefImageView.setImage(chefImage);
    }

    private void populateChefInfo(FoodBarData food) {
        ChefInfo chef = chefForProduct(food);

        chefNameLabel.setText(chefDisplayName(chef.name));
        chefDescriptionLabel.setText(chef.description);
        chefTag1Label.setText(chef.tag1);
        chefTag2Label.setText(chef.tag2);
        chefTag3Label.setText(chef.tag3);
        chefEmailLabel.setText("Email: " + chef.email);
        chefTelLabel.setText("Tel: " + chef.tel);
        chefRatingReviewButton.setText(
            String.format(
                "%s",
                NumberFormat.getIntegerInstance().format(chef.reviewCount)
            )
        );
        setStars(chefRatingStarsBox, chef.averageRating);

        setChefImage(chef.imageLocation, chef.name);
        configureChefFeatureIcons(chef.tag1, chef.tag2, chef.tag3);
    }

    @FXML
    private void showChefReviewsPlaceholder() {
        System.out.println("Chef reviews button clicked. Review page is not connected yet.");
    }

    private ChefInfo chefForProduct(FoodBarData food) {
        try {
            int chefId = chefIdForProduct(food.getProductId());
            for (ArrayList<String> row : DatabaseController.getData("Chef")) {
                if (safeInt(row, 0, -1) == chefId) {
                    return new ChefInfo(
                        valueAt(row, 1, "Chef Placeholder Name"),
                        valueAt(row, 3, "A creative culinary expert blending traditional recipes with modern health-focused meals."),
                        valueAt(row, 4, "Inspired by Asian Street Food"),
                        valueAt(row, 5, "Health-Driven Cooking"),
                        valueAt(row, 6, "18+ Years of Culinary Experience"),
                        valueAt(row, 7, "images/user.png"),
                        valueAt(row, 8, "chef@example.com"),
                        valueAt(row, 9, "+44 (0)1234 567890"),
                        chefRatingSummary(safeInt(row, 0, 1))
                    );
                }
            }
        } catch (ClassNotFoundException | SQLException exception) {
            System.out.println("Chef data could not be loaded: " + exception.getMessage());
        }

        return new ChefInfo(
            "Chef Placeholder Name",
            "A creative culinary expert blending traditional recipes with modern health-focused meals.",
            "Inspired by Asian Street Food",
            "Health-Driven Cooking",
            "18+ Years of Culinary Experience",
            "images/user.png",
            "chef@example.com",
            "+44 (0)1234 567890",
            new ChefRatingSummary(4.9, 239)
        );
    }

    private String chefDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Chef";
        }

        String cleanName = name.trim();
        return cleanName.toLowerCase(Locale.ENGLISH).startsWith("chef ")
            ? cleanName
            : "Chef " + cleanName;
    }

    private ChefRatingSummary chefRatingSummary(int chefId) {
        double totalRating = 0;
        int count = 0;

        try {
            for (ArrayList<String> row : DatabaseController.getData("ChefReview")) {
                // ChefReview columns: reviewID, chefID, reviewerName, rating, reviewText
                if (safeInt(row, 1, -1) == chefId) {
                    totalRating += safeDouble(row, 3, 0);
                    count++;
                }
            }
        } catch (ClassNotFoundException | SQLException exception) {
            System.out.println("Chef reviews could not be loaded: " + exception.getMessage());
        }

        if (count == 0) {
            return new ChefRatingSummary(4.9, 0);
        }

        return new ChefRatingSummary(totalRating / count, count);
    }

    private double safeDouble(ArrayList<String> row, int index, double fallback) {
        try {
            return Double.parseDouble(valueAt(row, index, String.valueOf(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private int chefIdForProduct(int productId) throws ClassNotFoundException, SQLException {
        for (ArrayList<String> row : DatabaseController.getData("Products")) {
            if (safeInt(row, 0, -1) == productId) {
                return safeInt(row, 11, 1);
            }
        }

        return 1;
    }

    private int safeInt(ArrayList<String> row, int index, int fallback) {
        try {
            return Integer.parseInt(valueAt(row, index, String.valueOf(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String valueAt(ArrayList<String> row, int index, String fallback) {
        if (row == null || index < 0 || index >= row.size()) {
            return fallback;
        }

        String value = row.get(index);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void configureChefFeatureIcons(String tag1, String tag2, String tag3) {
        setChefFeatureIcon(chefInspiredIcon, tag1);
        setChefFeatureIcon(chefHealthIcon, tag2);
        setChefFeatureIcon(chefExperienceIcon, tag3);
    }

    private void setChefFeatureIcon(ImageView target, String tagText) {
        if (target == null) {
            return;
        }

        String iconUrl = resolveExistingImageUrl(chefTagIconCandidates(tagText));
        if (iconUrl == null) {
            iconUrl = resolveExistingImageUrl(generalChefTagIconCandidates());
        }

        if (iconUrl == null) {
            target.setImage(null);
            return;
        }

        target.setImage(new Image(iconUrl, 38, 38, true, true, true));
        target.setVisible(true);
        target.setManaged(true);
    }

    private void setChefImage(String imageLocation, String chefName) {
        String imageUrl = resolveImageUrl(imageLocation);
        if (imageUrl == null) {
            imageUrl = resolveImageUrl("chefImage/" + slugFileName(chefName) + ".png");
        }
        if (imageUrl == null) {
            imageUrl = resolveImageUrl("images/chefs/" + slugFileName(chefName) + ".png");
        }
        if (imageUrl == null) {
            setDummyChefImage();
            return;
        }

        Image chefImage = new Image(imageUrl, 600, 860, false, true, false);
        if (chefImage.isError()) {
            System.err.println("Chef image failed: " + imageLocation + " -> "
                + imageUrl + " : " + chefImage.getException());
            setDummyChefImage();
            return;
        }

        chefImageView.setViewport(centerCropViewport(chefImage, 300, 430));
        chefImageView.setPreserveRatio(false);
        chefImageView.setFitWidth(300);
        chefImageView.setFitHeight(430);
        chefImageView.setImage(chefImage);
    }

    private String slugFileName(String value) {
        return normaliseCountry(value).replace(' ', '-');
    }

    private ArrayList<String> chefTagIconCandidates(String tagText) {
        ArrayList<String> candidates = new ArrayList<>();
        String cleanText = normaliseCountry(tagText);

        if (!cleanText.isEmpty()) {
            addTagImageCandidates(candidates, cleanText.replace(' ', '-'));
            addTagImageCandidates(candidates, cleanText.replace(' ', '_'));
            addTagImageCandidates(candidates, cleanText);
            for (String word : cleanText.split(" ")) {
                addTagImageCandidates(candidates, word);
            }
        }

        if (cleanText.contains("inspire")
                || cleanText.contains("creative")
                || cleanText.contains("innovation")
                || cleanText.contains("street")
                || cleanText.contains("fusion")) {
            addTagAliases(candidates, "inspire", "inspired", "inspiration", "creative", "creativity", "innovation", "street-food", "street_food", "fusion");
        }

        if (cleanText.contains("health")
                || cleanText.contains("healthy")
                || cleanText.contains("nutrition")
                || cleanText.contains("nutritious")
                || cleanText.contains("wellness")
                || cleanText.contains("balanced")
                || cleanText.contains("diet")
                || cleanText.contains("protein")
                || cleanText.contains("calorie")
                || cleanText.contains("fitness")) {
            addTagAliases(candidates, "nutrition-focused", "nutrition_focused", "healthy-recipe", "healthy_recipe", "health", "healthy", "nutrition", "wellness", "balanced", "diet", "protein", "calorie", "fitness", "health-driven", "health_driven");
        }

        if (cleanText.contains("experience")
                || cleanText.contains("expert")
                || cleanText.contains("professional")
                || cleanText.contains("master")
                || cleanText.contains("veteran")
                || cleanText.contains("year")
                || cleanText.contains("award")
                || cleanText.contains("trained")) {
            addTagAliases(candidates, "experience", "expert", "professional", "master", "veteran", "years", "award", "trained", "culinary-experience", "culinary_experience", "experienced-chef", "experienced_chef");
        }

        if (cleanText.contains("asian")
                || cleanText.contains("japanese")
                || cleanText.contains("korean")
                || cleanText.contains("thai")
                || cleanText.contains("burmese")
                || cleanText.contains("chinese")
                || cleanText.contains("indian")
                || cleanText.contains("street food")) {
            addTagAliases(candidates, "asian", "japanese", "korean", "thai", "burmese", "chinese", "indian", "street-food", "street_food");
        }

        if (cleanText.contains("fresh")
                || cleanText.contains("organic")
                || cleanText.contains("local")
                || cleanText.contains("seasonal")
                || cleanText.contains("ingredient")
                || cleanText.contains("sustainable")
                || cleanText.contains("farm")) {
            addTagAliases(candidates, "fresh", "organic", "local", "seasonal", "ingredient", "ingredients", "sustainable", "farm");
        }

        if (cleanText.contains("spice")
                || cleanText.contains("spicy")
                || cleanText.contains("flavour")
                || cleanText.contains("flavor")
                || cleanText.contains("aroma")
                || cleanText.contains("bold")) {
            addTagAliases(candidates, "spice", "spicy", "flavour", "flavor", "aroma", "bold");
        }

        if (cleanText.contains("bakery")
                || cleanText.contains("baker")
                || cleanText.contains("baking")
                || cleanText.contains("bread")
                || cleanText.contains("pastry")
                || cleanText.contains("dessert")) {
            addTagAliases(candidates, "bakery", "baker", "baking", "bread", "pastry", "dessert");
        }

        if (cleanText.contains("classic")
                || cleanText.contains("technique")
                || cleanText.contains("traditional")
                || cleanText.contains("heritage")
                || cleanText.contains("bistro")) {
            addTagAliases(candidates, "classic", "technique", "traditional", "heritage", "bistro");
        }

        if (cleanText.contains("comfort")
                || cleanText.contains("hearty")
                || cleanText.contains("homestyle")
                || cleanText.contains("mentor")
                || cleanText.contains("generous")) {
            addTagAliases(candidates, "comfort", "comfort-food", "comfort_food", "hearty", "homestyle", "mentor", "generous");
        }

        if (cleanText.contains("vegan")
                || cleanText.contains("vegetarian")
                || cleanText.contains("plant")
                || cleanText.contains("halal")
                || cleanText.contains("gluten")
                || cleanText.contains("pescatarian")) {
            addTagAliases(candidates, "vegan", "vegetarian", "plant-based", "plant_based", "halal", "gluten-free", "gluten_free", "pescatarian");
        }

        if (cleanText.contains("chef")
                || cleanText.contains("cooking")
                || cleanText.contains("culinary")
                || cleanText.contains("recipe")
                || cleanText.contains("kitchen")) {
            addTagAliases(candidates, "chef", "cooking", "culinary", "recipe", "kitchen");
        }

        return candidates;
    }

    private ArrayList<String> generalChefTagIconCandidates() {
        ArrayList<String> candidates = new ArrayList<>();
        addTagAliases(candidates, "chef", "cooking", "culinary", "recipe", "kitchen", "experience");
        return candidates;
    }

    private void addTagAliases(ArrayList<String> candidates, String... aliases) {
        for (String alias : aliases) {
            addTagImageCandidates(candidates, alias);
        }
    }

    private void addTagImageCandidates(ArrayList<String> candidates, String fileName) {
        candidates.add("/ProductPage/ProductPage/tags/" + fileName + ".png");
        candidates.add("/ProductPage/ProductPage/tags/" + fileName + ".jpg");
        candidates.add("/ProductPage/ProductPage/tags/" + fileName + ".jpeg");
        candidates.add("/ProductPage/ProductPage/tags/" + fileName + ".webp");
        candidates.add("/ProductPage/ProductPage/images/tags/" + fileName + ".png");
        candidates.add("/ProductPage/ProductPage/images/tags/" + fileName + ".jpg");
        candidates.add("/ProductPage/ProductPage/images/tags/" + fileName + ".jpeg");
        candidates.add("/ProductPage/ProductPage/images/tags/" + fileName + ".webp");
    }

    private void selectSpiceChoice(int spiceLevel) {
        for (Node node : spiceChoiceBox.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) node;
                button.setSelected(button.getText().equals(String.valueOf(spiceLevel)));
            }
        }
    }

    private void configureToggleGroup(Parent parent, ToggleGroup group) {
        if (parent == null) {
            return;
        }

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) node;
                button.setToggleGroup(group);
            }
        }
    }

    private void configurePortionPricing() {
        if (portionChoiceBox == null) {
            return;
        }

        for (Node node : portionChoiceBox.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) node;
                double price = button.getText().toLowerCase(Locale.ENGLISH).contains("large")
                    ? 2.00
                    : 0.00;

                button.setUserData(new IngredientOption(button.getText(), price));
                button.selectedProperty().addListener(
                    (observable, oldValue, newValue) -> updateCartControls()
                );
            }
        }
    }

    private void selectToggleByText(Parent parent, String text) {
        if (parent == null) {
            return;
        }

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) node;
                button.setSelected(button.getText().equals(text));
            }
        }
    }

    private void populateIngredientChoices(FoodBarData food) {
        ArrayList<IngredientOption> baseOptions = savedOptionsFor(food, "Base");
        ArrayList<IngredientOption> proteinOptions = savedOptionsFor(food, "Protein");
        ArrayList<IngredientOption> addOnOptions = savedOptionsFor(food, "Add-On");

        populateCheckOptions(baseChoiceBox, baseOptions.isEmpty() ? baseOptionsFor(food) : baseOptions, true);
        populateCheckOptions(proteinChoiceBox, proteinOptions.isEmpty() ? proteinOptionsFor(food) : proteinOptions, true);
        populateAddOns(food, addOnOptions);
        updateCartControls();
    }

    private void populateCheckOptions(
            VBox target,
            ArrayList<IngredientOption> options,
            boolean selectIncluded) {
        target.getChildren().clear();

        for (int index = 0; index < options.size(); index++) {
            IngredientOption ingredient = options.get(index);
            CheckBox option = new CheckBox(optionText(ingredient));
            option.setUserData(ingredient);
            option.getStyleClass().add("ingredient-check");
            option.setSelected(selectIncluded && (ingredient.selectedByDefault || (!hasDefaultOption(options) && index == 0)));
            option.selectedProperty().addListener((observable, oldValue, newValue) -> updateCartControls());
            target.getChildren().add(option);
        }
    }

    private void populateAddOns(FoodBarData food, ArrayList<IngredientOption> savedOptions) {
        addOnColumnLeft.getChildren().clear();
        addOnColumnRight.getChildren().clear();

        ArrayList<IngredientOption> options = savedOptions.isEmpty() ? addOnOptionsFor(food) : savedOptions;
        for (int index = 0; index < options.size(); index++) {
            CheckBox option = new CheckBox(optionText(options.get(index)));
            option.setUserData(options.get(index));
            option.getStyleClass().add("ingredient-check");
            option.setSelected(options.get(index).selectedByDefault);
            option.selectedProperty().addListener((observable, oldValue, newValue) -> updateCartControls());

            if (index % 2 == 0) {
                addOnColumnLeft.getChildren().add(option);
            } else {
                addOnColumnRight.getChildren().add(option);
            }
        }
    }

    private ArrayList<IngredientOption> savedOptionsFor(FoodBarData food, String group) {
        ArrayList<IngredientOption> options = new ArrayList<>();
        try {
            for (SellerProductRepository.ProductIngredientSelection selection
                    : SellerProductRepository.loadDefaultIngredientsForProduct(food.getProductId())) {
                if (group.equals(selection.optionGroup)) {
                    options.add(new IngredientOption(
                        selection.optionName,
                        selection.extraCost,
                        selection.selectedByDefault
                    ));
                }
            }
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
        }
        return options;
    }

    private boolean hasDefaultOption(ArrayList<IngredientOption> options) {
        for (IngredientOption option : options) {
            if (option.selectedByDefault) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<IngredientOption> baseOptionsFor(FoodBarData food) {
        ArrayList<IngredientOption> options = new ArrayList<>();
        String details = foodDetails(food);

        if (details.contains("kimchi fried rice")) {
            addOption(options, "Short-grain rice", 0);
            addOption(options, "Brown rice", 0.50);
            addOption(options, "Cauliflower rice", 0.80);
        } else if (details.contains("biryani")) {
            addOption(options, "Basmati rice", 0);
            addOption(options, "Brown basmati rice", 0.50);
            addOption(options, "Jeera rice", 0.60);
        } else if (details.contains("poke") || details.contains("sushi")) {
            addOption(options, "Sushi rice", 0);
            addOption(options, "Brown sushi rice", 0.50);
            addOption(options, "Cauliflower rice", 0.80);
        } else if (details.contains("noodle") || details.contains("pad thai") || details.contains("ramen") || details.contains("pho")) {
            addOption(options, "Rice noodles", 0);
            addOption(options, "Glass noodles", 0.50);
            addOption(options, "Wholewheat noodles", 0.60);
        } else if (details.contains("flatbread") || details.contains("wrap")) {
            addOption(options, "Flatbread", 0);
            addOption(options, "Wholemeal wrap", 0.50);
            addOption(options, "Rice bowl base", 0.80);
        } else if (details.contains("salad") || details.contains("quinoa")) {
            addOption(options, "Quinoa", 0);
            addOption(options, "Mixed greens", 0);
            addOption(options, "Wild rice", 0.70);
        } else {
            addOption(options, "Jasmine rice", 0);
            addOption(options, "Brown rice", 0.50);
            addOption(options, "Wild rice", 0.70);
            addOption(options, "Cauliflower rice", 0.80);
        }

        return options;
    }

    private ArrayList<IngredientOption> proteinOptionsFor(FoodBarData food) {
        ArrayList<IngredientOption> options = new ArrayList<>();
        String details = foodDetails(food);

        if (details.contains("kimchi fried rice")) {
            addOption(options, "Fried egg", 0);
            addOption(options, "Tofu cubes", 0.80);
            addOption(options, "Chicken thigh strips", 1.50);
        } else if (details.contains("salmon")) {
            addOption(options, "Grilled salmon fillet", 0);
            addOption(options, "Soft-boiled egg", 1.00);
            addOption(options, "Edamame beans", 0.80);
        } else if (details.contains("tuna")) {
            addOption(options, "Sushi-grade tuna cubes", 0);
            addOption(options, "Soft-boiled egg", 1.00);
            addOption(options, "Teriyaki tofu", 0.90);
        } else if (details.contains("prawn") || details.contains("shrimp")) {
            addOption(options, "Tiger prawns", 0);
            addOption(options, "Chicken breast strips", 1.20);
            addOption(options, "Firm tofu", 0.80);
        } else if (details.contains("biryani")) {
            addOption(options, "Spiced chicken thigh", 0);
            addOption(options, "Boiled egg", 0.80);
            addOption(options, "Paneer cubes", 1.20);
        } else if (details.contains("beef")) {
            addOption(options, "Marinated beef slices", 0);
            addOption(options, "Chicken breast strips", 1.00);
            addOption(options, "Teriyaki tofu", 0.90);
        } else if (details.contains("vegan") || details.contains("vegetarian")) {
            addOption(options, "Firm tofu", 0);
            addOption(options, "Edamame beans", 0.80);
            addOption(options, "Fried egg", 1.00);
        } else {
            addOption(options, "Chicken breast strips", 0);
            addOption(options, "Fried egg", 1.00);
            addOption(options, "Firm tofu", 0.80);
        }

        return options;
    }

    private ArrayList<IngredientOption> addOnOptionsFor(FoodBarData food) {
        ArrayList<IngredientOption> options = new ArrayList<>();
        for (ProductIngredientOptionRules.Option option : ProductIngredientOptionRules.fallbackAddOns(foodDetails(food))) {
            addOption(options, option.name, option.extraPrice);
        }
        return options;
    }

    private String foodDetails(FoodBarData food) {
        return normaliseCountry(
            food.getProductName() + " "
                + food.getDietary() + " "
                + food.getHealthGoal() + " "
                + food.getCuisine() + " "
                + food.getCountry() + " "
                + food.getShortDescription() + " "
                + food.getExtendedDescription()
        );
    }

    private void addOption(ArrayList<IngredientOption> options, String name, double extraPrice) {
        for (IngredientOption option : options) {
            if (option.name.equals(name)) {
                return;
            }
        }
        options.add(new IngredientOption(name, extraPrice));
    }

    private String optionText(IngredientOption option) {
        if (option.extraPrice <= 0) {
            return option.name;
        }
        return String.format("%s (+\u00A3%,.2f)", option.name, option.extraPrice);
    }

    private double currentItemPrice() {
        if (currentFood == null) {
            return 0;
        }

        double total = currentFood.getPrice();
        total += selectedIngredientTotal(baseChoiceBox);
        total += selectedIngredientTotal(proteinChoiceBox);
        total += selectedIngredientTotal(addOnColumnLeft);
        total += selectedIngredientTotal(addOnColumnRight);
        total += selectedPortionPrice();
        return total;
    }

    private String currentCartDetailSummary() {
        String base = selectedIngredientNames(baseChoiceBox);
        String protein = selectedIngredientNames(proteinChoiceBox);
        String addOns = selectedIngredientNames(addOnColumnLeft, addOnColumnRight);
        String spice = selectedToggleText(spiceToggleGroup, String.valueOf(currentFood.getSpiceLevel()));
        String portion = selectedToggleText(portionToggleGroup, "Regular");
        String orderType = subscriptionSelected ? "Weekly" : "One-time";

        return "Food: " + currentFood.getProductName()
            + " | Quantity: " + quantity
            + " | Order: " + orderType
            + " | Base: " + base
            + " | Protein: " + protein
            + " | Add-ons: " + addOns
            + " | Spice level: " + spice
            + " | Portion: " + portion
            + " | Item price: \u00A3" + String.format("%,.2f", currentItemPrice());
    }

    private String selectedToggleText(ToggleGroup group, String fallback) {
        if (group == null || group.getSelectedToggle() == null) {
            return fallback;
        }

        Object selected = group.getSelectedToggle();
        if (selected instanceof ToggleButton) {
            return ((ToggleButton) selected).getText();
        }

        return fallback;
    }

    private String selectedIngredientNames(VBox... containers) {
        ArrayList<String> selectedNames = new ArrayList<>();

        for (VBox container : containers) {
            if (container == null) {
                continue;
            }

            for (Node node : container.getChildren()) {
                if (!(node instanceof CheckBox)) {
                    continue;
                }

                CheckBox option = (CheckBox) node;
                if (!option.isSelected()) {
                    continue;
                }

                Object data = option.getUserData();
                if (data instanceof IngredientOption) {
                    selectedNames.add(((IngredientOption) data).name);
                } else {
                    selectedNames.add(option.getText());
                }
            }
        }

        if (selectedNames.isEmpty()) {
            return "None";
        }

        return String.join(", ", selectedNames);
    }

    private double selectedIngredientTotal(VBox container) {
        double total = 0;
        for (Node node : container.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox option = (CheckBox) node;
                if (option.isSelected() && option.getUserData() instanceof IngredientOption) {
                    total += ((IngredientOption) option.getUserData()).extraPrice;
                }
            }
        }
        return total;
    }

    private double selectedPortionPrice() {
        for (Node node : portionChoiceBox.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton option = (ToggleButton) node;
                if (option.isSelected() && option.getUserData() instanceof IngredientOption) {
                    return ((IngredientOption) option.getUserData()).extraPrice;
                }
            }
        }
        return 0;
    }

    private void updateAllergenAlert(FoodBarData food) {
        ArrayList<String> allergens = detectAllergens(food);

        allergenAlertLabel.getStyleClass().remove("allergen-warning");
        allergenAlertLabel.getStyleClass().remove("allergen-clear");

        if (allergens.isEmpty()) {
            allergenAlertLabel.setText("No major allergens detected.");
            allergenAlertLabel.getStyleClass().add("allergen-clear");
            return;
        }

        allergenAlertLabel.setText(
            "Alert: contains or may contain " + String.join(", ", allergens) + "."
        );
        allergenAlertLabel.getStyleClass().add("allergen-warning");
    }

    private ArrayList<String> detectAllergens(FoodBarData food) {
        ArrayList<String> allergens = new ArrayList<>();
        String details = " " + normaliseCountry(
            food.getProductName() + " "
                + food.getShortDescription() + " "
                + food.getExtendedDescription() + " "
                + food.getDietary() + " "
                + food.getCuisine()
        ) + " ";

        addAllergenIfMatched(allergens, details, "Egg", " egg ", " eggs ");
        addAllergenIfMatched(allergens, details, "Soy", " soy ", " tofu ", " miso ", " edamame ");
        addAllergenIfMatched(allergens, details, "Sesame", " sesame ", " tahini ");
        addAllergenIfMatched(allergens, details, "Peanut", " peanut ", " peanuts ");
        addAllergenIfMatched(allergens, details, "Tree nuts", " almond ", " cashew ", " walnut ", " pistachio ", " hazelnut ");
        addAllergenIfMatched(allergens, details, "Milk", " milk ", " cheese ", " cream ", " butter ", " yoghurt ", " yogurt ", " mozzarella ");
        addAllergenIfMatched(allergens, details, "Fish", " fish ", " salmon ", " tuna ", " cod ", " anchovy ");
        addAllergenIfMatched(allergens, details, "Shellfish", " shrimp ", " prawn ", " crab ", " lobster ", " shellfish ");

        if (!details.contains("gluten free")) {
            addAllergenIfMatched(allergens, details, "Gluten", " wheat ", " gluten ", " noodle ", " noodles ", " pasta ", " bread ", " flour ");
        }

        return allergens;
    }

    private void addAllergenIfMatched(
            ArrayList<String> allergens,
            String details,
            String allergen,
            String... keywords) {
        if (allergens.contains(allergen)) {
            return;
        }

        for (String keyword : keywords) {
            if (details.contains(keyword)) {
                allergens.add(allergen);
                return;
            }
        }
    }

    private void populateNutritionTable(FoodBarData food) {
        nutritionTable.getChildren().clear();
        addNutritionRow(0, "Nutrient", "Amount", true);
        addNutritionRow(1, "Calories", String.format("%.0f kcal", food.getTotalCalories()), false);
        addNutritionRow(2, "Protein", String.format("%.0fg", food.getTotalProtein()), false);
        addNutritionRow(3, "Carbohydrates", String.format("%.0fg", food.getTotalCarbohydrates()), false);
        addNutritionRow(4, "Sugars", String.format("%.0fg", food.getTotalSugars()), false);
        addNutritionRow(5, "Fat", String.format("%.0fg", food.getTotalFat()), false);
        addNutritionRow(6, "Saturated Fat", String.format("%.0fg", food.getTotalSaturatedFat()), false);
        addNutritionRow(7, "Fiber", String.format("%.0fg", food.getTotalFiber()), false);
        addNutritionRow(8, "Sodium", String.format("%.0fmg", food.getTotalSodium()), false);
    }

    private void addNutritionRow(int row, String nutrient, String amount, boolean header) {
        Label nutrientLabel = nutritionCell(nutrient, header, true);
        Label amountLabel = nutritionCell(amount, header, false);

        if (row == 0) {
            nutrientLabel.getStyleClass().add("nutrition-top-left");
            amountLabel.getStyleClass().add("nutrition-top-right");
        }

        if (row == 8) {
            nutrientLabel.getStyleClass().add("nutrition-bottom-left");
            amountLabel.getStyleClass().add("nutrition-bottom-right");
        }

        nutritionTable.add(nutrientLabel, 0, row);
        nutritionTable.add(amountLabel, 1, row);
    }

    private Label nutritionCell(String text, boolean header, boolean leftColumn) {
        Label label = new Label(text);
        label.setMinWidth(155);
        label.setPrefWidth(155);
        label.setMinHeight(header ? 28 : 24);
        label.setWrapText(true);
        label.getStyleClass().add(header ? "nutrition-header-cell" : "nutrition-cell");
        label.getStyleClass().add(leftColumn ? "nutrition-left-cell" : "nutrition-right-cell");
        return label;
    }

    private void showCurrentImage() {
        if (images.isEmpty()) {
            foodImageView.setImage(null);
            imageCounterLabel.setText("0 / 0");
            return;
        }

        String imageUrl = resolveImageUrl(images.get(imageIndex));
        if (imageUrl != null) {
            foodImageView.setImage(new Image(imageUrl, 640, 640, false, true, true));
        }

        imageCounterLabel.setText(
            String.format("%d / %d", imageIndex + 1, images.size())
        );
    }

    private void setRating(double averageRating) {
        setStars(ratingStarsBox, averageRating);
    }

    private void setStars(HBox target, double averageRating) {
        if (target == null) {
            return;
        }

        target.getChildren().clear();
        double roundedRating = Math.round(
            Math.max(0, Math.min(averageRating, 5)) * 2
        ) / 2.0;

        for (int position = 1; position <= 5; position++) {
            Label star = new Label(roundedRating >= position ? "\u2605" : "\u2606");
            star.getStyleClass().add(
                roundedRating >= position ? "rating-star" : "rating-star-empty"
            );
            target.getChildren().add(star);
        }
    }

    private void setSpiceLevel(int spiceLevel) {
        if (topSpiceBox == null) {
            return;
        }

        topSpiceBox.getChildren().clear();

        if (spiceLevel <= 0) {
            Label mildLabel = new Label("Mild");
            mildLabel.getStyleClass().add("top-summary-text");
            topSpiceBox.getChildren().add(mildLabel);
            return;
        }

        String spicyUrl = resolveExistingImageUrl(spicyIconCandidates());
        if (spicyUrl == null) {
            Label fallbackLabel = new Label(spiceText(spiceLevel));
            fallbackLabel.getStyleClass().add("top-summary-text");
            topSpiceBox.getChildren().add(fallbackLabel);
            return;
        }

        for (int index = 0; index < spiceLevel; index++) {
            ImageView spicyIcon = new ImageView(new Image(spicyUrl, 18, 18, true, true, true));
            spicyIcon.setFitWidth(18);
            spicyIcon.setFitHeight(18);
            spicyIcon.setPreserveRatio(true);
            spicyIcon.setSmooth(true);
            spicyIcon.getStyleClass().add("spice-icon");
            topSpiceBox.getChildren().add(spicyIcon);
        }
    }

    private ArrayList<String> spicyIconCandidates() {
        ArrayList<String> candidates = new ArrayList<>();
        candidates.add("/ProductPage/ProductPage/images/spicy.png");
        candidates.add("/ProductPage/ProductPage/images/spicy.jpg");
        candidates.add("/ProductPage/ProductPage/images/spicy.jpeg");
        candidates.add("/ProductPage/ProductPage/images/spicy/spicy.png");
        candidates.add("/ProductPage/ProductPage/images/spicy/spicy.jpg");
        candidates.add("/ProductPage/ProductPage/images/spicy/spicy.jpeg");
        return candidates;
    }

    private void setCountryFlag(String country) {
        String flagUrl = resolveImageUrl(flagPath(country));

        if (flagUrl == null) {
            countryFlagImage.setImage(null);
            countryFlagImage.setVisible(false);
            countryFlagImage.setManaged(false);
            return;
        }

        Image flag = new Image(flagUrl, 220, 220, true, true, false);
        countryFlagImage.setViewport(centerSquareViewport(flag));
        countryFlagImage.setPreserveRatio(false);
        countryFlagImage.setFitWidth(86);
        countryFlagImage.setFitHeight(86);
        countryFlagImage.setImage(flag);
        countryFlagImage.setVisible(true);
        countryFlagImage.setManaged(true);
    }

    private void showSuggestions(FoodBarData currentFood) {
        suggestionList.getChildren().clear();

        try {
            ArrayList<FoodBarData> suggestions = new ArrayList<>();
            for (FoodBarData food : FoodBarRepository.loadActiveProducts()) {
                if (food.getProductId() == currentFood.getProductId()) {
                    continue;
                }

                if (sameText(food.getCountry(), currentFood.getCountry())
                        || sameText(food.getCuisine(), currentFood.getCuisine())
                        || sameText(food.getHealthGoal(), currentFood.getHealthGoal())) {
                    suggestions.add(food);
                }
            }

            if (suggestions.isEmpty()) {
                suggestionList.getChildren().add(
                    suggestionLabel("No similar meals found yet.")
                );
                return;
            }

            for (FoodBarData suggestion : suggestions) {
                suggestionList.getChildren().add(suggestionCard(suggestion));
            }
        } catch (ClassNotFoundException | SQLException exception) {
            suggestionList.getChildren().add(
                suggestionLabel("Suggestions unavailable.")
            );
        }
    }

    private Label suggestionLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("suggestion-empty");
        return label;
    }

    private Button suggestionCard(FoodBarData suggestion) {
        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(210);
        previewImage.setFitHeight(150);
        previewImage.setPreserveRatio(false);
        previewImage.setSmooth(true);
        previewImage.getStyleClass().add("suggestion-image");

        Rectangle imageClip = new Rectangle(210, 150);
        imageClip.setArcWidth(28);
        imageClip.setArcHeight(28);
        previewImage.setClip(imageClip);

        String imageUrl = resolveImageUrl(suggestion.getProductImageLocation());
        if (imageUrl != null) {
            previewImage.setImage(new Image(imageUrl, 376, 176, false, true, true));
        }

        Label nameLabel = new Label(suggestion.getProductName());
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("suggestion-name");

        Label ratingLabel = new Label(
            String.format(
                "\u2605 %.1f (%s)",
                suggestion.getAverageRating(),
                NumberFormat.getIntegerInstance().format(
                    suggestion.getRatingCount()
                )
            )
        );
        ratingLabel.getStyleClass().add("suggestion-meta");

        Label priceLabel = new Label(
            String.format("\u00A3%,.2f", suggestion.getPrice())
        );
        priceLabel.getStyleClass().add("suggestion-price");

        HBox metaRow = new HBox(8, ratingLabel, priceLabel);
        metaRow.getStyleClass().add("suggestion-meta-row");

        VBox content = new VBox(6, previewImage, nameLabel, metaRow);
        content.getStyleClass().add("suggestion-content");

        Button card = new Button();
        card.setGraphic(content);
        card.setMnemonicParsing(false);
        card.setMinWidth(226);
        card.setPrefWidth(226);
        card.setMinHeight(242);
        card.setPrefHeight(242);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnAction(event -> showFood(suggestion));
        card.getStyleClass().add("suggestion-card");
        return card;
    }

    private void setActiveTab(Button selectedButton) {
        Button[] tabButtons = {
            overviewTabButton,
            ingredientsTabButton,
            chefTabButton,
            reviewsTabButton
        };

        for (Button button : tabButtons) {
            button.getStyleClass().remove("detail-breadcrumb-active");
        }
        selectedButton.getStyleClass().add("detail-breadcrumb-active");
    }

    private void setTagIcon(
            Label label,
            String value,
            String fallbackName,
            String explicitIconLocation) {
        String iconUrl = resolveImageUrl(explicitIconLocation);
        if (iconUrl == null) {
            iconUrl = resolveExistingImageUrl(iconCandidates(value, fallbackName));
        }

        if (iconUrl == null) {
            label.setGraphic(null);
            return;
        }

        ImageView icon = new ImageView(new Image(iconUrl, 20, 20, true, true, true));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);
        label.setGraphic(icon);
    }

    private ArrayList<String> iconCandidates(String value, String fallbackName) {
        ArrayList<String> candidates = new ArrayList<>();
        String fileName = normaliseCountry(value).replace(' ', '-');
        String underscoredFileName = normaliseCountry(value).replace(' ', '_');

        if (!fileName.isEmpty()) {
            addIconFileCandidates(candidates, fileName);
        }

        if (!underscoredFileName.isEmpty() && !underscoredFileName.equals(fileName)) {
            addIconFileCandidates(candidates, underscoredFileName);
        }

        for (String alias : iconAliases(value, fallbackName)) {
            addIconFileCandidates(candidates, alias);
        }

        addIconFileCandidates(candidates, fallbackName);
        return candidates;
    }

    private void addIconFileCandidates(ArrayList<String> candidates, String fileName) {
        candidates.add("/ProductPage/ProductPage/images/" + fileName + ".png");
        candidates.add("/ProductPage/ProductPage/images/" + fileName + ".jpg");
        candidates.add("/ProductPage/ProductPage/images/" + fileName + ".jpeg");
        candidates.add("/ProductPage/ProductPage/images/icons/" + fileName + ".png");
        candidates.add("/ProductPage/ProductPage/images/icons/" + fileName + ".jpg");
        candidates.add("/ProductPage/ProductPage/images/icons/" + fileName + ".jpeg");
    }

    private ArrayList<String> iconAliases(String value, String fallbackName) {
        ArrayList<String> aliases = new ArrayList<>();
        String cleanValue = normaliseCountry(value);

        if (cleanValue.contains("vegetarian")
                || cleanValue.contains("vegan")
                || cleanValue.contains("halal")
                || cleanValue.contains("gluten")
                || cleanValue.contains("pescatarian")) {
            aliases.add("dietary");
        }
        if (cleanValue.contains("weight")
                || cleanValue.contains("protein")
                || cleanValue.contains("balanced")) {
            aliases.add("healthy-goals");
            aliases.add("healthy_goals");
            aliases.add("health-goal");
            aliases.add("health_goal");
        }
        if (cleanValue.contains("asian")
                || cleanValue.contains("western")
                || cleanValue.contains("middle")
                || cleanValue.contains("mediterranean")) {
            aliases.add("cuisine");
        }

        aliases.add(fallbackName);
        return aliases;
    }

    private String resolveExistingImageUrl(ArrayList<String> candidates) {
        for (String candidate : candidates) {
            String imageUrl = resolveImageUrl(candidate);
            if (imageUrl != null) {
                return imageUrl;
            }
        }

        return null;
    }

    private Rectangle2D centerSquareViewport(Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        double size = Math.min(width, height);

        return new Rectangle2D(
            (width - size) / 2,
            (height - size) / 2,
            size,
            size
        );
    }

    private Rectangle2D centerCropViewport(
            Image image,
            double targetWidth,
            double targetHeight) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        if (imageWidth <= 0 || imageHeight <= 0) {
            return null;
        }

        double targetRatio = targetWidth / targetHeight;
        double imageRatio = imageWidth / imageHeight;
        double cropWidth = imageWidth;
        double cropHeight = imageHeight;

        if (imageRatio > targetRatio) {
            cropWidth = imageHeight * targetRatio;
        } else {
            cropHeight = imageWidth / targetRatio;
        }

        return new Rectangle2D(
            (imageWidth - cropWidth) / 2,
            (imageHeight - cropHeight) / 2,
            cropWidth,
            cropHeight
        );
    }

    private boolean sameText(String first, String second) {
        String firstValue = normaliseCountry(first);
        String secondValue = normaliseCountry(second);
        return !firstValue.isEmpty() && firstValue.equals(secondValue);
    }

    private String flagPath(String country) {
        String countryCode = countryCode(country);

        if (countryCode == null) {
            return null;
        }

        return "world_flags/" + countryCode + ".png";
    }

    private String countryCode(String country) {
        String value = normaliseCountry(country);

        if (value.isEmpty()) {
            return null;
        }

        if (value.equals("uk")
                || value.equals("u k")
                || value.equals("britain")
                || value.equals("great britain")
                || value.equals("united kingdom")
                || value.equals("england")
                || value.equals("scotland")
                || value.equals("wales")) {
            return "gb";
        }
        if (value.equals("usa")
                || value.equals("u s a")
                || value.equals("us")
                || value.equals("u s")
                || value.equals("america")
                || value.equals("united states")
                || value.equals("united states of america")) {
            return "us";
        }
        if (value.equals("korea")
                || value.equals("south korea")
                || value.equals("republic of korea")) {
            return "kr";
        }
        if (value.equals("north korea")
                || value.equals("democratic peoples republic of korea")) {
            return "kp";
        }
        if (value.contains("myanmar") || value.contains("burma")) {
            return "mm";
        }
        if (value.equals("russia")) {
            return "ru";
        }
        if (value.equals("vietnam")) {
            return "vn";
        }
        if (value.equals("laos")) {
            return "la";
        }
        if (value.equals("iran")) {
            return "ir";
        }
        if (value.equals("syria")) {
            return "sy";
        }
        if (value.equals("moldova")) {
            return "md";
        }
        if (value.equals("bolivia")) {
            return "bo";
        }
        if (value.equals("venezuela")) {
            return "ve";
        }
        if (value.equals("tanzania")) {
            return "tz";
        }
        if (value.equals("palestine")) {
            return "ps";
        }
        if (value.equals("czechia")
                || value.equals("czech republic")) {
            return "cz";
        }
        if (value.equals("western")) {
            return "gb";
        }

        for (String isoCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", isoCode);
            if (value.equals(normaliseCountry(locale.getDisplayCountry(Locale.ENGLISH)))) {
                return isoCode.toLowerCase(Locale.ENGLISH);
            }
        }

        return null;
    }

    private String normaliseCountry(String value) {
        if (value == null) {
            return "";
        }

        return value
            .trim()
            .toLowerCase(Locale.ENGLISH)
            .replace("&", " and ")
            .replaceAll("[^a-z0-9]+", " ")
            .trim();
    }

    private String spiceText(int spiceLevel) {
        if (spiceLevel <= 0) {
            return "Mild";
        }

        StringBuilder text = new StringBuilder();
        for (int index = 0; index < spiceLevel; index++) {
            text.append("\u2668");
        }
        return text.toString();
    }

    private String resolveImageUrl(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }

        String cleanLocation = location.trim().replace('\\', '/');
        if (cleanLocation.startsWith("@")) {
            cleanLocation = cleanLocation.substring(1);
        }

        if (cleanLocation.startsWith("http://")
                || cleanLocation.startsWith("https://")
                || cleanLocation.startsWith("file:")) {
            return cleanLocation;
        }

        ArrayList<String> candidates = imageCandidates(cleanLocation);
        for (String candidate : candidates) {
            String imageUrl = resolveSingleImageCandidate(candidate);
            if (imageUrl != null) {
                return imageUrl;
            }
        }

        System.err.println("Image not found: " + location + " tried " + candidates);
        return null;
    }

    private ArrayList<String> imageCandidates(String location) {
        ArrayList<String> candidates = new ArrayList<>();
        String cleanLocation = location == null ? "" : location.trim().replace('\\', '/');
        if (cleanLocation.startsWith("@")) {
            cleanLocation = cleanLocation.substring(1);
        }
        if (cleanLocation.isEmpty()) {
            return candidates;
        }

        addImageCandidate(candidates, cleanLocation);
        addImageCandidate(candidates, "/" + cleanLocation);

        String fileName = cleanLocation;
        int slashIndex = cleanLocation.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < cleanLocation.length() - 1) {
            fileName = cleanLocation.substring(slashIndex + 1);
        }

        addImageCandidate(candidates, "images/" + cleanLocation);
        addImageCandidate(candidates, "/ProductPage/ProductPage/" + cleanLocation);
        addImageCandidate(candidates, "/ProductPage/ProductPage/images/" + cleanLocation);

        addImageCandidate(candidates, "chefImage/" + fileName);
        addImageCandidate(candidates, "images/chefImage/" + fileName);
        addImageCandidate(candidates, "images/chefs/" + fileName);
        addImageCandidate(candidates, "/ProductPage/ProductPage/chefImage/" + fileName);
        addImageCandidate(candidates, "/ProductPage/ProductPage/images/chefImage/" + fileName);
        addImageCandidate(candidates, "/ProductPage/ProductPage/images/chefs/" + fileName);

        addImageCandidate(candidates, "world_flags/" + fileName);
        addImageCandidate(candidates, "images/world_flags/" + fileName);
        addImageCandidate(candidates, "images/flags/" + fileName);
        addImageCandidate(candidates, "/ProductPage/ProductPage/world_flags/" + fileName);
        addImageCandidate(candidates, "/ProductPage/ProductPage/images/world_flags/" + fileName);
        addImageCandidate(candidates, "/ProductPage/ProductPage/images/flags/" + fileName);
        return candidates;
    }

    private void addImageCandidate(ArrayList<String> candidates, String candidate) {
        if (candidate != null && !candidate.trim().isEmpty()
                && !candidates.contains(candidate)) {
            candidates.add(candidate);
        }
    }

    private String resolveSingleImageCandidate(String candidate) {
        URL resource = candidate.startsWith("/")
            ? getClass().getResource(candidate)
            : getClass().getResource(candidate);

        if (resource == null && !candidate.startsWith("/")) {
            resource = getClass().getResource("/ProductPage/ProductPage/" + candidate);
        }

        if (resource != null) {
            return resource.toExternalForm();
        }

        File file = new File(candidate);
        return file.exists() ? file.toURI().toString() : null;
    }

    private static class ChefInfo {
        private final String name;
        private final String description;
        private final String tag1;
        private final String tag2;
        private final String tag3;
        private final String imageLocation;
        private final String email;
        private final String tel;
        private final double averageRating;
        private final int reviewCount;

        private ChefInfo(
                String name,
                String description,
                String tag1,
                String tag2,
                String tag3,
                String imageLocation,
                String email,
                String tel,
                ChefRatingSummary ratingSummary) {
            this.name = name;
            this.description = description;
            this.tag1 = tag1;
            this.tag2 = tag2;
            this.tag3 = tag3;
            this.imageLocation = imageLocation;
            this.email = email;
            this.tel = tel;
            this.averageRating = ratingSummary.averageRating;
            this.reviewCount = ratingSummary.reviewCount;
        }
    }

    private static class ChefRatingSummary {
        private final double averageRating;
        private final int reviewCount;

        private ChefRatingSummary(double averageRating, int reviewCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }
    }

    private static class IngredientOption {
        private final String name;
        private final double extraPrice;
        private final boolean selectedByDefault;

        private IngredientOption(String name, double extraPrice) {
            this(name, extraPrice, false);
        }

        private IngredientOption(String name, double extraPrice, boolean selectedByDefault) {
            this.name = name;
            this.extraPrice = extraPrice;
            this.selectedByDefault = selectedByDefault;
        }
    }
}
