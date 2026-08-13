package ProductPage.ProductPage;

import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class FoodBarController {

    private static final double FOOD_IMAGE_WIDTH = 142;
    private static final double FOOD_IMAGE_HEIGHT = 136;
    private static final double FOOD_IMAGE_RADIUS = 20;
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    @FXML private Button foodCardButton;
    @FXML private ImageView foodImage;
    @FXML private ImageView dietaryIcon;
    @FXML private ImageView goalIcon;
    @FXML private ImageView cuisineIcon;
    @FXML private HBox ratingStarsBox;

    @FXML private Label foodNameLabel;
    @FXML private Label availabilityLabel;
    @FXML private Label priceLabel;
    @FXML private Label calorieLabel;
    @FXML private Label reviewCountLabel;
    @FXML private Label dietaryLabel;
    @FXML private Label goalLabel;
    @FXML private Label cuisineLabel;
    @FXML private Label descriptionLabel;
    private FoodBarData data;
    private Consumer<FoodBarData> selectionHandler;

    @FXML
    private void initialize() {
        Rectangle imageClip = new Rectangle(
            FOOD_IMAGE_WIDTH,
            FOOD_IMAGE_HEIGHT
        );
        imageClip.setArcWidth(FOOD_IMAGE_RADIUS * 2);
        imageClip.setArcHeight(FOOD_IMAGE_RADIUS * 2);
        foodImage.setClip(imageClip);
    }

    public void setData(FoodBarData data) {
        this.data = data;
        foodNameLabel.setText(data.getProductName());
        boolean unavailable = !data.isAvailable();
        availabilityLabel.setText(data.getAvailabilityMessage());
        availabilityLabel.setVisible(unavailable);
        availabilityLabel.setManaged(unavailable);
        availabilityLabel.setTooltip(unavailable
                ? new Tooltip(data.getAvailabilityMessage()) : null);
        foodCardButton.getStyleClass().remove("food-card-unavailable");
        if (unavailable) {
            foodCardButton.getStyleClass().add("food-card-unavailable");
        }
        priceLabel.setText(String.format("\u00A3%,.2f", data.getPrice()));
        if (calorieLabel != null) {
            calorieLabel.setText(
                String.format("%.0f kcal", data.getTotalCalories())
            );
        }
        descriptionLabel.setText(data.getShortDescription());
        setRating(data.getAverageRating(), data.getRatingCount());

        dietaryLabel.setText(data.getDietary());
        goalLabel.setText(data.getHealthGoal());
        cuisineLabel.setText(data.getCuisine());

        setImage(foodImage, data.getProductImageLocation());
        setImage(dietaryIcon, data.getDietaryIconLocation());
        setImage(goalIcon, data.getGoalIconLocation());
        setImage(cuisineIcon, data.getCuisineIconLocation());
    }

    public void setCaloriesVisible(boolean visible) {
        if (calorieLabel == null) {
            return;
        }
        calorieLabel.setVisible(visible);
        calorieLabel.setManaged(visible);
    }

    public void setSelectionHandler(Consumer<FoodBarData> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    @FXML
    private void handleFoodClick() {
        if (selectionHandler != null && data != null && data.isAvailable()) {
            selectionHandler.accept(data);
        }
    }

    private void setRating(double averageRating, int ratingCount) {
        ratingStarsBox.getChildren().clear();

        double safeRating = Math.max(0, Math.min(averageRating, 5));
        double roundedRating = Math.round(safeRating * 2.0) / 2.0;

        for (int position = 1; position <= 5; position++) {
            if (roundedRating >= position) {
                ratingStarsBox.getChildren().add(createFullStar());
            } else if (roundedRating >= position - 0.5) {
                ratingStarsBox.getChildren().add(createHalfStar());
            } else {
                ratingStarsBox.getChildren().add(createEmptyStar());
            }
        }

        reviewCountLabel.setText(
            String.format(
                "%.1f (%s)",
                safeRating,
                NumberFormat.getIntegerInstance().format(ratingCount)
            )
        );
    }

    private Label createFullStar() {
        Label star = new Label("\u2605");
        star.getStyleClass().add("rating-star");
        configureStarLabel(star);
        return star;
    }

    private Label createEmptyStar() {
        Label star = new Label("\u2606");
        star.getStyleClass().add("rating-star-empty");
        configureStarLabel(star);
        return star;
    }

    private StackPane createHalfStar() {
        StackPane star = new StackPane();
        star.setMinSize(22, 24);
        star.setPrefSize(22, 24);
        star.setMaxSize(22, 24);

        Label empty = createEmptyStar();
        Label filled = createFullStar();
        filled.setClip(new Rectangle(11, 24));

        StackPane.setAlignment(filled, javafx.geometry.Pos.CENTER_LEFT);
        star.getChildren().addAll(empty, filled);
        return star;
    }

    private void configureStarLabel(Label star) {
        star.setTextOverrun(OverrunStyle.CLIP);
        star.setMinSize(22, 24);
        star.setPrefSize(22, 24);
        star.setMaxSize(22, 24);
    }

    private void setImage(ImageView view, String location) {
        if (location == null || location.trim().isEmpty()) {
            view.setImage(null);
            view.setVisible(false);
            view.setManaged(false);
            return;
        }

        String url = resolveImageUrl(location.trim());

        if (url == null) {
            view.setImage(null);
            view.setVisible(false);
            view.setManaged(false);
            return;
        }

        double requestedWidth = view == foodImage
            ? FOOD_IMAGE_WIDTH * 2
            : Math.max(36, view.getFitWidth() * 2);
        double requestedHeight = view == foodImage
            ? FOOD_IMAGE_HEIGHT * 2
            : Math.max(36, view.getFitHeight() * 2);
        String cacheKey = url + "|" + requestedWidth + "x" + requestedHeight;

        Image image = IMAGE_CACHE.computeIfAbsent(
            cacheKey,
            key -> new Image(
                url,
                requestedWidth,
                requestedHeight,
                false,
                true,
                false
            )
        );

        view.setImage(image);
        view.setVisible(true);
        view.setManaged(true);
    }

    private String resolveImageUrl(String location) {
        String cleanLocation = location.replace('\\', '/');
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
            if (file.exists()) {
                return file.toURI().toString();
            }
        }

        System.err.println("Image not found: " + location + " tried " + candidates);
        return null;
    }

    private ArrayList<String> imageCandidates(String location) {
        ArrayList<String> candidates = new ArrayList<>();
        addImageCandidate(candidates, location);
        addImageCandidate(candidates, "/" + location);

        String fileName = location;
        int slashIndex = location.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < location.length() - 1) {
            fileName = location.substring(slashIndex + 1);
        }

        addImageCandidate(candidates, "images/" + location);
        addImageCandidate(candidates, "/ProductPage/ProductPage/" + location);
        addImageCandidate(candidates, "/ProductPage/ProductPage/images/" + location);
        addImageCandidate(candidates, "chefImage/" + fileName);
        addImageCandidate(candidates, "images/chefImage/" + fileName);
        addImageCandidate(candidates, "images/chefs/" + fileName);
        addImageCandidate(candidates, "world_flags/" + fileName);
        addImageCandidate(candidates, "images/world_flags/" + fileName);
        addImageCandidate(candidates, "images/flags/" + fileName);
        return candidates;
    }

    private void addImageCandidate(ArrayList<String> candidates, String candidate) {
        if (candidate != null && !candidate.trim().isEmpty()
                && !candidates.contains(candidate)) {
            candidates.add(candidate);
        }
    }
}
