package ProductPage.ProductPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;
import javafx.util.Duration;
import java.sql.SQLException;

public class PrimaryController {
    private static final double HIGHLY_RATED_MINIMUM = 4.5;
    private static final double SORT_POPUP_X_FROM_BUTTON_RIGHT = -160;
    private static final double SORT_POPUP_TOP_GAP = 18;

	@FXML
    private Label accountNameLabel;

    @FXML
    private Label orderCountLabel;

    @FXML
    private VBox sortPopup;

    @FXML
    private Button sortButton;
    
    @FXML
    private TextField minPriceField;

    @FXML
    private TextField maxPriceField;

    @FXML
    private Label priceErrorLabel;
    
    @FXML
    private VBox foodList;

    @FXML
    private TextField searchField;

    @FXML
    private Pane japaneseDecorLayer;

    @FXML
    private Button highlyRatedButton;

    @FXML
    private Button healthGoalButton;

    @FXML
    private Button lowCalorieButton;

    @FXML
    private Button budgetPicksButton;

    @FXML
    private VBox healthPlanner;

    @FXML
    private Button weightLossPlanButton;

    @FXML
    private Button highProteinPlanButton;

    @FXML
    private Button balancedPlanButton;

    @FXML
    private Button showHealthPlanButton;

    @FXML
    private Label healthPlanFocusLabel;

    @FXML
    private VBox healthPlanMealList;

    @FXML
    private Label healthPlanTotalLabel;

    @FXML
    private TextField healthCalorieField;

    @FXML
    private Label healthCalorieErrorLabel;

    @FXML
    private Spinner<Integer> healthMealSplitSpinner;

    @FXML
    private Label healthPerMealLabel;

    @FXML
    private Button lightCalorieButton;

    @FXML
    private Button standardCalorieButton;

    @FXML
    private Button fuelCalorieButton;

    @FXML
    private VBox foodDetailView;

    @FXML
    private ImageView detailFoodImage;

    @FXML
    private Label detailFoodNameLabel;

    @FXML
    private Label detailPriceLabel;

    @FXML
    private Label detailReviewLabel;

    @FXML
    private HBox detailStarsBox;

    @FXML
    private Label detailDietaryLabel;

    @FXML
    private Label detailGoalLabel;

    @FXML
    private Label detailCuisineLabel;

    @FXML
    private Label detailDescriptionLabel;

    @FXML
    private Label detailNutritionLabel;

    @FXML
    private Label detailImageCounterLabel;

    @FXML
    private Button previousDetailImageButton;

    @FXML
    private Button nextDetailImageButton;

    @FXML
    private ToggleButton popupHighlyRatedToggle;

    @FXML
    private ToggleButton popupNewestToggle;

    @FXML
    private ToggleButton popupLowCalorieToggle;

    @FXML
    private ToggleButton popupCheapestToggle;

    @FXML
    private ToggleButton vegetarianToggle;

    @FXML
    private ToggleButton veganToggle;

    @FXML
    private ToggleButton halalToggle;

    @FXML
    private ToggleButton glutenFreeToggle;

    @FXML
    private ToggleButton pescatarianToggle;

    @FXML
    private ToggleButton weightLossFilterToggle;

    @FXML
    private ToggleButton highProteinFilterToggle;

    @FXML
    private ToggleButton balancedMealsFilterToggle;

    @FXML
    private ToggleButton asianToggle;

    @FXML
    private ToggleButton southeastAsianToggle;

    @FXML
    private ToggleButton southAsianToggle;

    @FXML
    private ToggleButton middleEasternToggle;

    @FXML
    private ToggleButton westernToggle;

    @FXML
    private ToggleButton mediterraneanToggle;

    private final ArrayList<FoodBarData> allFoodData = new ArrayList<>();
    private final Map<String, Parent> foodCardCache = new HashMap<>();
    private final ArrayList<FoodBarData> suggestedHealthPlan =
        new ArrayList<>();
    private boolean showingHighlyRated;
    private boolean showingLowCalorie;
    private boolean showingBudgetPicks;
    private boolean healthPlanApplied;
    private String selectedHealthGoal;
    private String selectedHealthFocus;
    private Button selectedHealthGoalButton;
    private final ArrayList<String> detailImages = new ArrayList<>();
    private int detailImageIndex;
    
    public void setFirstName(String f_name) {
        accountNameLabel.setText("Hungry, " + f_name + "?");
    }

    public void setOrderCount(int count) {
        orderCountLabel.setText(String.valueOf(count));
    }

    @FXML
    private void toggleSortPopup() {
        closeFoodDetails();
        closeHealthPlanner();
        boolean show = !sortPopup.isVisible();
        sortPopup.setVisible(show);
        sortPopup.setManaged(false);
        if (show) {
            sortPopup.applyCss();
            sortPopup.autosize();
            Platform.runLater(() -> {
                movePopupFromOwnerRightEdge(
                        sortPopup,
                        sortButton,
                        SORT_POPUP_X_FROM_BUTTON_RIGHT,
                        SORT_POPUP_TOP_GAP
                );
                sortPopup.toFront();
            });
        }
    }
    
    @FXML
    private void applyFilters() {
        String minText = minPriceField.getText().trim();
        String maxText = maxPriceField.getText().trim();
        Double min = null;
        Double max = null;

        try {
            if (!minText.isEmpty()) {
                min = Double.parseDouble(minText);
            }

            if (!maxText.isEmpty()) {
                max = Double.parseDouble(maxText);
            }

            if ((min != null && min < 0) || (max != null && max < 0)) {
                showPriceError("Prices cannot be negative.");
                return;
            }

            if (min != null && max != null && min > max) {
                showPriceError("Minimum price cannot exceed maximum price.");
                return;
            }

            clearPriceError();
            applyPopupFilterResults(min, max);
            hideSortPopup();

        } catch (NumberFormatException e) {
            showPriceError("Enter valid numeric prices.");
        }
    }

    private void applyPopupFilterResults(Double minPrice, Double maxPrice) {
        closeFoodDetails();
        closeHealthPlanner();
        clearQuickFilterButtons();

        String dietary = getSelectedDietaryFilter();
        String healthGoal = getSelectedHealthGoalFilter();
        String cuisine = getSelectedCuisineFilter();
        String sort = getSelectedSortFilter();

        ArrayList<FoodBarData> filteredFoods = new ArrayList<>();
        for (FoodBarData data : allFoodData) {
            if (minPrice != null && data.getPrice() < minPrice) {
                continue;
            }

            if (maxPrice != null && data.getPrice() > maxPrice) {
                continue;
            }

            if (!matchesFilter(data.getDietary(), dietary)) {
                continue;
            }

            if (!matchesFilter(data.getHealthGoal(), healthGoal)) {
                continue;
            }

            if (!matchesFilter(data.getCuisine(), cuisine)) {
                continue;
            }

            if ("Highly Rated".equals(sort)
                    && data.getAverageRating() < HIGHLY_RATED_MINIMUM) {
                continue;
            }

            filteredFoods.add(data);
        }

        boolean showCalories = "Low-Calorie".equals(sort);
        sortFilteredFoods(filteredFoods, sort);
        renderFoodCards(filteredFoods, showCalories);
        updateActiveBreadcrumbForPopupSort(sort);
    }

    private void sortFilteredFoods(ArrayList<FoodBarData> foods, String sort) {
        if ("Highly Rated".equals(sort)) {
            foods.sort(
                (first, second) -> Double.compare(
                    second.getAverageRating(),
                    first.getAverageRating()
                )
            );
        } else if ("Newest".equals(sort)) {
            foods.sort(
                (first, second) -> compareCreatedDate(second, first)
            );
        } else if ("Low-Calorie".equals(sort)) {
            foods.sort(
                (first, second) -> Double.compare(
                    first.getTotalCalories(),
                    second.getTotalCalories()
                )
            );
        } else if ("Cheapest".equals(sort)) {
            foods.sort(
                (first, second) -> Double.compare(
                    first.getPrice(),
                    second.getPrice()
                )
            );
        }
    }

    private int compareCreatedDate(FoodBarData first, FoodBarData second) {
        LocalDate firstDate = parseCreatedDate(first.getCreatedDate());
        LocalDate secondDate = parseCreatedDate(second.getCreatedDate());

        int dateComparison = firstDate.compareTo(secondDate);
        if (dateComparison != 0) {
            return dateComparison;
        }

        return Integer.compare(first.getProductId(), second.getProductId());
    }

    private LocalDate parseCreatedDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return LocalDate.MIN;
        }

        String cleanDate = dateText.trim();
        DateTimeFormatter[] formats = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter format : formats) {
            try {
                return LocalDate.parse(cleanDate, format);
            } catch (DateTimeParseException ignored) {
            }
        }

        return LocalDate.MIN;
    }

    private void updateActiveBreadcrumbForPopupSort(String sort) {
        showingHighlyRated = "Highly Rated".equals(sort);
        showingLowCalorie = "Low-Calorie".equals(sort);
        showingBudgetPicks = "Cheapest".equals(sort);

        if (showingHighlyRated) {
            highlyRatedButton.getStyleClass().add("active-filter");
        } else if (showingLowCalorie) {
            lowCalorieButton.getStyleClass().add("active-filter");
        } else if (showingBudgetPicks) {
            budgetPicksButton.getStyleClass().add("active-filter");
        }
    }

    private boolean matchesFilter(String actualValue, String selectedValue) {
        return selectedValue == null
            || selectedValue.equalsIgnoreCase(actualValue);
    }

    private String getSelectedSortFilter() {
        if (popupHighlyRatedToggle.isSelected()) {
            return "Highly Rated";
        }
        if (popupNewestToggle.isSelected()) {
            return "Newest";
        }
        if (popupLowCalorieToggle.isSelected()) {
            return "Low-Calorie";
        }
        if (popupCheapestToggle.isSelected()) {
            return "Cheapest";
        }
        return null;
    }

    private String getSelectedDietaryFilter() {
        if (vegetarianToggle.isSelected()) {
            return "Vegetarian";
        }
        if (veganToggle.isSelected()) {
            return "Vegan";
        }
        if (halalToggle.isSelected()) {
            return "Halal";
        }
        if (glutenFreeToggle.isSelected()) {
            return "Gluten-Free";
        }
        if (pescatarianToggle.isSelected()) {
            return "Pescatarian";
        }
        return null;
    }

    private String getSelectedHealthGoalFilter() {
        if (weightLossFilterToggle.isSelected()) {
            return "Weight Loss";
        }
        if (highProteinFilterToggle.isSelected()) {
            return "High Protein";
        }
        if (balancedMealsFilterToggle.isSelected()) {
            return "Balanced Meals";
        }
        return null;
    }

    private String getSelectedCuisineFilter() {
        if (asianToggle.isSelected()) {
            return "Asian";
        }
        if (southeastAsianToggle.isSelected()) {
            return "Southeast Asian";
        }
        if (southAsianToggle.isSelected()) {
            return "South Asian";
        }
        if (middleEasternToggle.isSelected()) {
            return "Middle Eastern";
        }
        if (westernToggle.isSelected()) {
            return "Western";
        }
        if (mediterraneanToggle.isSelected()) {
            return "Mediterranean";
        }
        return null;
    }

    private void showPriceError(String message) {
        priceErrorLabel.setText(message);
        priceErrorLabel.setVisible(true);
        priceErrorLabel.setManaged(true);
    }

    private void clearPriceError() {
        priceErrorLabel.setText("");
        priceErrorLabel.setVisible(false);
        priceErrorLabel.setManaged(false);
    }

    private void hideSortPopup() {
        sortPopup.setVisible(false);
        sortPopup.setManaged(false);
    }

    private void movePopupFromOwnerRightEdge(VBox popup, Node owner, double xOffsetFromButtonRight, double yOffset) {
        if (popup == null || owner == null || popup.getParent() == null) {
            return;
        }

        Bounds ownerBounds = owner.localToScene(owner.getBoundsInLocal());
        Point2D ownerBottomRight = popup.getParent().sceneToLocal(ownerBounds.getMaxX(), ownerBounds.getMaxY());

        double popupWidth = popup.prefWidth(-1) > 0 ? popup.prefWidth(-1) : popup.getBoundsInLocal().getWidth();
        double popupHeight = popup.prefHeight(-1) > 0 ? popup.prefHeight(-1) : popup.getBoundsInLocal().getHeight();
        double parentWidth = popup.getParent().getLayoutBounds().getWidth();
        double parentHeight = popup.getParent().getLayoutBounds().getHeight();

        double x = ownerBottomRight.getX() + xOffsetFromButtonRight;
        double y = ownerBottomRight.getY() + yOffset;

        x = Math.max(0, Math.min(x, Math.max(0, parentWidth - popupWidth)));
        y = Math.max(0, Math.min(y, Math.max(0, parentHeight - popupHeight)));

        popup.setTranslateX(0);
        popup.setTranslateY(0);
        popup.relocate(x, y);
    }
    
   

    private void loadFoodCards() {
        try {
            allFoodData.clear();
            allFoodData.addAll(FoodBarRepository.loadActiveProducts());
            foodCardCache.clear();
            renderFoodCards(allFoodData);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleHighlyRated() {
        closeFoodDetails();
        closeHealthPlanner();
        clearQuickFilterButtons();
        showingHighlyRated = !showingHighlyRated;
        showingLowCalorie = false;
        showingBudgetPicks = false;

        if (showingHighlyRated) {
            ArrayList<FoodBarData> highlyRated = new ArrayList<>();

            for (FoodBarData data : allFoodData) {
                if (data.getAverageRating() >= HIGHLY_RATED_MINIMUM) {
                    highlyRated.add(data);
                }
            }

            highlyRated.sort(
                (first, second) -> Double.compare(
                    second.getAverageRating(),
                    first.getAverageRating()
                )
            );
            renderFoodCards(highlyRated);
            highlyRatedButton.getStyleClass().add("active-filter");
        } else {
            renderFoodCards(allFoodData);
        }
    }

    @FXML
    private void toggleLowCalorie() {
        closeFoodDetails();
        closeHealthPlanner();
        clearQuickFilterButtons();
        showingLowCalorie = !showingLowCalorie;
        showingHighlyRated = false;
        showingBudgetPicks = false;

        if (showingLowCalorie) {
            ArrayList<FoodBarData> lowCalorie = new ArrayList<>(allFoodData);
            lowCalorie.sort(
                (first, second) -> Double.compare(
                    first.getTotalCalories(),
                    second.getTotalCalories()
                )
            );
            renderFoodCards(lowCalorie, true);
            lowCalorieButton.getStyleClass().add("active-filter");
        } else {
            renderFoodCards(allFoodData);
        }
    }

    @FXML
    private void toggleBudgetPicks() {
        closeFoodDetails();
        closeHealthPlanner();
        clearQuickFilterButtons();
        showingBudgetPicks = !showingBudgetPicks;
        showingHighlyRated = false;
        showingLowCalorie = false;

        if (showingBudgetPicks) {
            ArrayList<FoodBarData> budgetPicks = new ArrayList<>(allFoodData);
            budgetPicks.sort(
                (first, second) -> Double.compare(
                    first.getPrice(),
                    second.getPrice()
                )
            );
            renderFoodCards(budgetPicks);
            budgetPicksButton.getStyleClass().add("active-filter");
        } else {
            renderFoodCards(allFoodData);
        }
    }

    @FXML
    private void toggleHealthPlanner() {
        closeFoodDetails();
        hideSortPopup();
        showingHighlyRated = false;
        showingLowCalorie = false;
        showingBudgetPicks = false;
        highlyRatedButton.getStyleClass().remove("active-filter");
        lowCalorieButton.getStyleClass().remove("active-filter");
        budgetPicksButton.getStyleClass().remove("active-filter");
        boolean show = !healthPlanner.isVisible();
        healthPlanner.setVisible(show);
        healthPlanner.setManaged(show);

        if (show) {
            healthGoalButton.getStyleClass().add("active-filter");
        } else if (!healthPlanApplied) {
            healthGoalButton.getStyleClass().remove("active-filter");
        }
    }

    @FXML
    private void closeHealthPlanner() {
        healthPlanner.setVisible(false);
        healthPlanner.setManaged(false);

        if (!healthPlanApplied) {
            healthGoalButton.getStyleClass().remove("active-filter");
        }
    }

    @FXML
    private void selectWeightLossPlan() {
        selectHealthGoal(
            "Weight Loss",
            "Lighter meals selected to support a calorie-conscious routine.",
            weightLossPlanButton
        );
    }

    @FXML
    private void selectHighProteinPlan() {
        selectHealthGoal(
            "High Protein",
            "Protein-focused meals selected to support training and recovery.",
            highProteinPlanButton
        );
    }

    @FXML
    private void selectBalancedPlan() {
        selectHealthGoal(
            "Balanced Meals",
            "Well-rounded meals selected for steady energy and everyday nutrition.",
            balancedPlanButton
        );
    }

    private void selectHealthGoal(
            String goal,
            String focus,
            Button selectedButton) {

        selectedHealthGoal = goal;
        selectedHealthFocus = focus;
        selectedHealthGoalButton = selectedButton;
        createHealthPlan(goal, focus, selectedButton);
    }

    @FXML
    private void selectLightCalories() {
        setCalorieTarget(readPresetCalories(lightCalorieButton));
    }

    @FXML
    private void selectStandardCalories() {
        setCalorieTarget(readPresetCalories(standardCalorieButton));
    }

    @FXML
    private void selectFuelCalories() {
        setCalorieTarget(readPresetCalories(fuelCalorieButton));
    }

    private int readPresetCalories(Button presetButton) {
        return Integer.parseInt(presetButton.getText());
    }

    private void setCalorieTarget(int calories) {
        healthCalorieField.setText(String.valueOf(calories));
        refreshHealthPlanCalories();
    }

    private void updateCaloriePresets() {
        int mealCount = healthMealSplitSpinner.getValue();
        int lightCalories = mealCount * 500;

        lightCalorieButton.setText(String.valueOf(lightCalories));
        standardCalorieButton.setText(String.valueOf(lightCalories + 500));
        fuelCalorieButton.setText(String.valueOf(lightCalories + 1000));
    }

    @FXML
    private void refreshHealthPlanCalories() {
        if (selectedHealthGoal != null) {
            createHealthPlan(
                selectedHealthGoal,
                selectedHealthFocus,
                selectedHealthGoalButton
            );
        }
    }

    private void createHealthPlan(
            String goal,
            String focus,
            Button selectedButton) {

        Double dailyCalorieTarget = readDailyCalorieTarget();
        if (dailyCalorieTarget == null) {
            clearUnavailablePlan();
            return;
        }
        int mealCount = healthMealSplitSpinner.getValue();
        double calorieTarget = dailyCalorieTarget / mealCount;
        healthPerMealLabel.setText(
            String.format("About %.0f kcal per meal", calorieTarget)
        );

        ArrayList<FoodBarData> matchingMeals = new ArrayList<>();
        for (FoodBarData data : allFoodData) {
            if (goal.equalsIgnoreCase(data.getHealthGoal())) {
                matchingMeals.add(data);
            }
        }

        setActiveHealthGoalButton(selectedButton);
        if (!isCalorieTargetPossible(
                matchingMeals,
                mealCount,
                dailyCalorieTarget)) {
            return;
        }

        suggestedHealthPlan.clear();
        suggestedHealthPlan.addAll(
            findClosestCaloriePlan(
                matchingMeals,
                mealCount,
                dailyCalorieTarget
            )
        );

        double actualCalories = getPlanCalories(suggestedHealthPlan);
        double allowedDifference = Math.max(
            200,
            dailyCalorieTarget * 0.15
        );
        if (Math.abs(actualCalories - dailyCalorieTarget)
                > allowedDifference) {
            showImpossiblePlan(
                String.format(
                    "No %d-meal %s plan is close enough to %.0f kcal. "
                        + "The closest available plan is %.0f kcal.",
                    mealCount,
                    goal.toLowerCase(),
                    dailyCalorieTarget,
                    actualCalories
                )
            );
            return;
        }

        hideCalorieError();
        healthPlanFocusLabel.setText(
            String.format(
                "%s Daily target: %.0f kcal across %d meals.",
                focus,
                dailyCalorieTarget,
                mealCount
            )
        );
        updateHealthPlanLabels();
        showHealthPlanButton.setDisable(suggestedHealthPlan.isEmpty());
    }

    private ArrayList<FoodBarData> findClosestCaloriePlan(
            ArrayList<FoodBarData> meals,
            int mealCount,
            double targetCalories) {

        ArrayList<Map<Integer, ArrayList<FoodBarData>>> combinations =
            new ArrayList<>();
        for (int count = 0; count <= mealCount; count++) {
            combinations.add(new HashMap<>());
        }
        combinations.get(0).put(0, new ArrayList<>());

        for (FoodBarData meal : meals) {
            int mealCalories = (int) Math.round(meal.getTotalCalories());
            for (int count = mealCount; count >= 1; count--) {
                Map<Integer, ArrayList<FoodBarData>> previous =
                    combinations.get(count - 1);
                Map<Integer, ArrayList<FoodBarData>> current =
                    combinations.get(count);

                for (Map.Entry<Integer, ArrayList<FoodBarData>> entry
                        : previous.entrySet()) {
                    int calorieTotal = entry.getKey() + mealCalories;
                    if (!current.containsKey(calorieTotal)) {
                        ArrayList<FoodBarData> plan =
                            new ArrayList<>(entry.getValue());
                        plan.add(meal);
                        current.put(calorieTotal, plan);
                    }
                }
            }
        }

        ArrayList<FoodBarData> closestPlan = new ArrayList<>();
        double smallestDifference = Double.MAX_VALUE;
        for (Map.Entry<Integer, ArrayList<FoodBarData>> entry
                : combinations.get(mealCount).entrySet()) {
            double difference = Math.abs(
                entry.getKey() - targetCalories
            );
            if (difference < smallestDifference) {
                smallestDifference = difference;
                closestPlan = entry.getValue();
            }
        }
        return closestPlan;
    }

    private boolean isCalorieTargetPossible(
            ArrayList<FoodBarData> matchingMeals,
            int mealCount,
            double dailyCalorieTarget) {

        if (matchingMeals.size() < mealCount) {
            showImpossiblePlan(
                String.format(
                    "This goal only has %d matching meals. "
                        + "Choose %d meals or fewer.",
                    matchingMeals.size(),
                    matchingMeals.size()
                )
            );
            return false;
        }

        matchingMeals.sort(
            (first, second) -> Double.compare(
                first.getTotalCalories(),
                second.getTotalCalories()
            )
        );

        double minimumCalories = 0;
        double maximumCalories = 0;
        for (int index = 0; index < mealCount; index++) {
            minimumCalories += matchingMeals.get(index).getTotalCalories();
            maximumCalories += matchingMeals
                .get(matchingMeals.size() - 1 - index)
                .getTotalCalories();
        }

        double allowedDifference = Math.max(
            200,
            dailyCalorieTarget * 0.15
        );
        if (dailyCalorieTarget < minimumCalories - allowedDifference
                || dailyCalorieTarget
                    > maximumCalories + allowedDifference) {
            showImpossiblePlan(
                String.format(
                    "A %d-meal %s plan can provide about %.0f-%.0f kcal. "
                        + "%.0f kcal is not currently possible.",
                    mealCount,
                    selectedHealthGoal.toLowerCase(),
                    minimumCalories,
                    maximumCalories,
                    dailyCalorieTarget
                )
            );
            return false;
        }

        return true;
    }

    private double getPlanCalories(ArrayList<FoodBarData> meals) {
        double totalCalories = 0;
        for (FoodBarData meal : meals) {
            totalCalories += meal.getTotalCalories();
        }
        return totalCalories;
    }

    private void showImpossiblePlan(String message) {
        clearUnavailablePlan();
        showCalorieError(message);
    }

    private void clearUnavailablePlan() {
        suggestedHealthPlan.clear();
        healthPlanMealList.getChildren().clear();
        healthPlanTotalLabel.setText("Plan unavailable");
        healthPlanFocusLabel.setText(
            "Try another calorie preset or reduce the meal count."
        );
        showHealthPlanButton.setDisable(true);
    }

    private Double readDailyCalorieTarget() {
        try {
            double target = Double.parseDouble(
                healthCalorieField.getText().trim()
            );

            if (target < 800 || target > 6000) {
                showCalorieError(
                    "Choose a daily calorie target from 800 to 6000."
                );
                return null;
            }

            hideCalorieError();
            return target;
        } catch (NumberFormatException exception) {
            showCalorieError("Enter a valid calorie number.");
            return null;
        }
    }

    private void showCalorieError(String message) {
        healthCalorieErrorLabel.setText(message);
        healthCalorieErrorLabel.setVisible(true);
        healthCalorieErrorLabel.setManaged(true);
    }

    private void hideCalorieError() {
        healthCalorieErrorLabel.setText("");
        healthCalorieErrorLabel.setVisible(false);
        healthCalorieErrorLabel.setManaged(false);
    }

    private void updateHealthPlanLabels() {
        healthPlanMealList.getChildren().clear();
        double total = 0;
        double totalCalories = 0;
        double totalProtein = 0;

        for (int index = 0; index < suggestedHealthPlan.size(); index++) {
            FoodBarData meal = suggestedHealthPlan.get(index);
            Label mealLabel = new Label(
                String.format(
                    "Meal %d   %s%n\u2605 %.1f   %.0f kcal   "
                        + "%.0fg protein   \u00A3%.2f",
                    index + 1,
                    meal.getProductName(),
                    meal.getAverageRating(),
                    meal.getTotalCalories(),
                    meal.getTotalProtein(),
                    meal.getPrice()
                )
            );
            mealLabel.setWrapText(true);
            mealLabel.setMaxWidth(Double.MAX_VALUE);
            mealLabel.getStyleClass().add("health-plan-meal");
            healthPlanMealList.getChildren().add(mealLabel);
            total += meal.getPrice();
            totalCalories += meal.getTotalCalories();
            totalProtein += meal.getTotalProtein();
        }

        healthPlanTotalLabel.setText(
            String.format(
                "Plan: %.0f kcal | %.0fg protein | \u00A3%.2f",
                totalCalories,
                totalProtein,
                total
            )
        );
    }

    private void setActiveHealthGoalButton(Button selectedButton) {
        Button[] buttons = {
            weightLossPlanButton,
            highProteinPlanButton,
            balancedPlanButton
        };

        for (Button button : buttons) {
            button.getStyleClass().remove("selected-health-goal");
        }
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("selected-health-goal");
        }
    }

    @FXML
    private void showSuggestedHealthPlan() {
        if (suggestedHealthPlan.isEmpty()) {
            return;
        }

        showingHighlyRated = false;
        highlyRatedButton.getStyleClass().remove("active-filter");
        showingLowCalorie = false;
        showingBudgetPicks = false;
        lowCalorieButton.getStyleClass().remove("active-filter");
        budgetPicksButton.getStyleClass().remove("active-filter");
        healthPlanApplied = false;
        healthGoalButton.getStyleClass().remove("active-filter");
        renderFoodCards(new ArrayList<>(suggestedHealthPlan));
        closeHealthPlanner();
    }

    @FXML
    private void resetHealthPlan() {
        suggestedHealthPlan.clear();
        healthPlanApplied = false;
        showingHighlyRated = false;
        showingLowCalorie = false;
        showingBudgetPicks = false;
        selectedHealthGoal = null;
        selectedHealthFocus = null;
        selectedHealthGoalButton = null;
        highlyRatedButton.getStyleClass().remove("active-filter");
        lowCalorieButton.getStyleClass().remove("active-filter");
        budgetPicksButton.getStyleClass().remove("active-filter");
        healthGoalButton.getStyleClass().remove("active-filter");
        showHealthPlanButton.setDisable(true);
        healthPlanFocusLabel.setText("Select a goal to create your plan.");
        healthPlanMealList.getChildren().clear();
        healthPlanTotalLabel.setText("Plan total: \u00A30.00");
        healthCalorieField.setText("2000");
        healthMealSplitSpinner.getValueFactory().setValue(3);
        healthPerMealLabel.setText("About 667 kcal per meal");
        hideCalorieError();
        setActiveHealthGoalButton(null);
        renderFoodCards(allFoodData);
        closeHealthPlanner();
    }

    private void renderFoodCards(ArrayList<FoodBarData> products) {
        renderFoodCards(products, false);
    }

    @FXML
    private void applySearch() {
        hideSortPopup();
        closeHealthPlanner();

        String query = searchField == null ? "" : searchField.getText();
        ArrayList<FoodBarData> results = searchFoods(query);
        renderFoodCards(results);
    }

    private ArrayList<FoodBarData> searchFoods(String query) {
        String cleanQuery = normaliseSearchText(query);
        if (cleanQuery.isEmpty()) {
            return new ArrayList<>(allFoodData);
        }

        ArrayList<FoodBarData> matches = new ArrayList<>();
        for (FoodBarData food : allFoodData) {
            if (matchesSearch(food, cleanQuery)) {
                matches.add(food);
            }
        }

        matches.sort(
            Comparator.comparingInt((FoodBarData food) -> searchScore(food, cleanQuery))
                .reversed()
                .thenComparing(FoodBarData::getProductName, String.CASE_INSENSITIVE_ORDER)
        );
        return matches;
    }

    private boolean matchesSearch(FoodBarData food, String cleanQuery) {
        String haystack = searchTextFor(food);
        if (haystack.contains(cleanQuery)) {
            return true;
        }

        String[] words = cleanQuery.split("\\s+");
        boolean allWordsIncluded = true;
        for (String word : words) {
            if (!word.isBlank() && !haystack.contains(word)) {
                allWordsIncluded = false;
                break;
            }
        }

        return allWordsIncluded
            || containsCharactersInOrder(
                cleanQuery.replace(" ", ""),
                haystack.replace(" ", "")
            );
    }

    private int searchScore(FoodBarData food, String cleanQuery) {
        String name = normaliseSearchText(food.getProductName());
        String haystack = searchTextFor(food);
        int score = 0;

        if (name.equals(cleanQuery)) {
            score += 10000;
        }
        if (name.startsWith(cleanQuery)) {
            score += 5000;
        }
        if (name.contains(cleanQuery)) {
            score += 3500;
        }
        if (haystack.contains(cleanQuery)) {
            score += 2000;
        }

        for (String word : cleanQuery.split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }
            if (name.contains(word)) {
                score += 450;
            } else if (haystack.contains(word)) {
                score += 180;
            }
        }

        score += commonPrefixLength(name, cleanQuery) * 60;
        score += orderedCharacterScore(cleanQuery.replace(" ", ""), haystack.replace(" ", ""));
        score -= Math.abs(name.length() - cleanQuery.length());
        return score;
    }

    private String searchTextFor(FoodBarData food) {
        return normaliseSearchText(
            safeText(food.getProductName()) + " "
                + safeText(food.getShortDescription()) + " "
                + safeText(food.getExtendedDescription()) + " "
                + safeText(food.getDietary()) + " "
                + safeText(food.getHealthGoal()) + " "
                + safeText(food.getCuisine()) + " "
                + safeText(food.getCountry())
        );
    }

    private String normaliseSearchText(String value) {
        return safeText(value)
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", " ")
            .trim()
            .replaceAll("\\s+", " ");
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private int commonPrefixLength(String left, String right) {
        int length = Math.min(left.length(), right.length());
        int index = 0;
        while (index < length && left.charAt(index) == right.charAt(index)) {
            index++;
        }
        return index;
    }

    private boolean containsCharactersInOrder(String needle, String haystack) {
        if (needle.isEmpty()) {
            return true;
        }
        int needleIndex = 0;
        for (int i = 0; i < haystack.length() && needleIndex < needle.length(); i++) {
            if (haystack.charAt(i) == needle.charAt(needleIndex)) {
                needleIndex++;
            }
        }
        return needleIndex == needle.length();
    }

    private int orderedCharacterScore(String needle, String haystack) {
        int score = 0;
        int needleIndex = 0;
        for (int i = 0; i < haystack.length() && needleIndex < needle.length(); i++) {
            if (haystack.charAt(i) == needle.charAt(needleIndex)) {
                score += 20;
                needleIndex++;
            }
        }
        return score;
    }

    private void renderFoodCards(
            ArrayList<FoodBarData> products,
            boolean showCalories) {
        foodList.getChildren().clear();

        if (products.isEmpty()) {
            Label emptyMessage = new Label("No foods match these filters.");
            emptyMessage.getStyleClass().add("empty-food-message");
            foodList.getChildren().add(emptyMessage);
            return;
        }

        for (FoodBarData data : products) {
            try {
                String cacheKey = data.getProductId()
                    + "|calories="
                    + showCalories;
                Parent card = foodCardCache.get(cacheKey);

                if (card == null) {
                    card = createFoodCard(data, showCalories);
                    foodCardCache.put(cacheKey, card);
                }

                foodList.getChildren().add(card);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private Parent createFoodCard(
            FoodBarData data,
            boolean showCalories) throws IOException {

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("FoodBarTemplate.fxml")
        );
        Parent card = loader.load();
        FoodBarController controller = loader.getController();
        controller.setData(data);
        controller.setCaloriesVisible(showCalories);
        controller.setSelectionHandler(this::showFoodDetails);
        return card;
    }

    private void showFoodDetails(FoodBarData data) {
        hideSortPopup();
        closeHealthPlanner();

        try {
        	URL viewFoodResource = getClass().getResource("/ProductPage/ProductPage/View Food.fxml");

            if (viewFoodResource == null) {
                throw new IOException("View Food.fxml not found in ProductPage/ProductPage/");
            }

            FXMLLoader loader = new FXMLLoader(viewFoodResource);
            Parent detailRoot = loader.load();

            ViewFoodController controller = loader.getController();
            controller.setFood(data);

            foodList.getScene().setRoot(detailRoot);

        } catch (Exception exception) {
            System.err.println("========== VIEW FOOD LOAD ERROR ==========");
            System.err.println("FXML URL: " + getClass().getResource("View Food.fxml"));

            if (data != null) {
                System.err.println("Product ID: " + data.getProductId());
                System.err.println("Product Name: " + data.getProductName());
            }

            System.err.println("Exception: " + exception.getClass().getName());
            System.err.println("Message: " + exception.getMessage());

            Throwable cause = exception.getCause();
            int depth = 1;
            while (cause != null) {
                System.err.println("Cause " + depth + ": " + cause.getClass().getName()
                    + " - " + cause.getMessage());
                cause = cause.getCause();
                depth++;
            }

            exception.printStackTrace();
            System.err.println("==========================================");
        }
    }

    @FXML
    private void closeFoodDetails() {
        foodDetailView.setVisible(false);
        foodDetailView.setManaged(false);
    }

    @FXML
    private void showPreviousDetailImage() {
        if (detailImages.isEmpty()) {
            return;
        }
        detailImageIndex =
            (detailImageIndex - 1 + detailImages.size()) % detailImages.size();
        showDetailImage();
    }

    @FXML
    private void showNextDetailImage() {
        if (detailImages.isEmpty()) {
            return;
        }
        detailImageIndex = (detailImageIndex + 1) % detailImages.size();
        showDetailImage();
    }

    private void showDetailImage() {
        if (detailImages.isEmpty()) {
            detailFoodImage.setImage(null);
            detailImageCounterLabel.setText("0 / 0");
            previousDetailImageButton.setDisable(true);
            nextDetailImageButton.setDisable(true);
            return;
        }

        setImage(detailFoodImage, detailImages.get(detailImageIndex), 472, 472);
        detailImageCounterLabel.setText(
            String.format("%d / %d", detailImageIndex + 1, detailImages.size())
        );
        boolean hasMultipleImages = detailImages.size() > 1;
        previousDetailImageButton.setDisable(!hasMultipleImages);
        nextDetailImageButton.setDisable(!hasMultipleImages);
    }

    private void setDetailRating(double averageRating) {
        detailStarsBox.getChildren().clear();
        double roundedRating = Math.round(Math.max(0, Math.min(averageRating, 5)) * 2) / 2.0;

        for (int position = 1; position <= 5; position++) {
            Label star = new Label(roundedRating >= position ? "\u2605" : "\u2606");
            star.getStyleClass().add(
                roundedRating >= position ? "rating-star" : "rating-star-empty"
            );
            detailStarsBox.getChildren().add(star);
        }
    }

    private void setImage(
            ImageView imageView,
            String location,
            double requestedWidth,
            double requestedHeight) {

        String imageUrl = resolveImageUrl(location);
        if (imageUrl == null) {
            imageView.setImage(null);
            return;
        }

        imageView.setImage(
            new Image(imageUrl, requestedWidth, requestedHeight, false, true, true)
        );
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

    private void clearQuickFilterButtons() {
        healthPlanApplied = false;
        healthGoalButton.getStyleClass().remove("active-filter");
        highlyRatedButton.getStyleClass().remove("active-filter");
        lowCalorieButton.getStyleClass().remove("active-filter");
        budgetPicksButton.getStyleClass().remove("active-filter");
    }
    
    @FXML
    private void initialize() {
        if (accountNameLabel.getText() == null
                || accountNameLabel.getText().trim().isEmpty()) {
            accountNameLabel.setText("Hungry, Jasper?");
        }
        setOrderCount(CartStore.getTotalQuantity());

        Rectangle detailImageClip = new Rectangle(236, 236);
        detailImageClip.setArcWidth(24);
        detailImageClip.setArcHeight(24);
        detailFoodImage.setClip(detailImageClip);

        healthMealSplitSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 8, 3)
        );
        healthMealSplitSpinner.valueProperty().addListener(
            (observable, oldValue, newValue) -> {
                updateCaloriePresets();
                refreshHealthPlanCalories();
            }
        );
        updateCaloriePresets();
        loadFoodCards();
        Platform.runLater(this::startJapaneseFoodDecoration);
    }

    private void startJapaneseFoodDecoration() {
        if (japaneseDecorLayer == null || !japaneseDecorLayer.getChildren().isEmpty()) {
            return;
        }

        for (int i = 0; i < 14; i++) {
            addSakuraPetal(i, i % 2 == 1);
        }
    }

    private void addSakuraPetal(int index, boolean rightSide) {
        Label petal = new Label("\u273F");
        petal.getStyleClass().add("sakura-petal");
        petal.setStyle(
            "-fx-font-size: " + (16 + (index % 5) * 3) + "px;" +
            "-fx-opacity: " + (0.38 + (index % 4) * 0.10) + ";"
        );
        petal.setLayoutY(220 - index * 57);
        positionSideNode(
            petal,
            rightSide,
            18 + (index % 7) * 24,
            34
        );
        japaneseDecorLayer.getChildren().add(petal);

        TranslateTransition fall = new TranslateTransition(
            Duration.seconds(6.5 + (index % 6) * 1.15),
            petal
        );
        fall.setByY(980);
        double drift = 22 + (index % 5) * 12;
        fall.setByX(rightSide ? -drift : drift);
        fall.setCycleCount(TranslateTransition.INDEFINITE);
        fall.setInterpolator(Interpolator.LINEAR);
        fall.setDelay(Duration.seconds(index * 0.55));

        RotateTransition spin = new RotateTransition(
            Duration.seconds(3.5 + (index % 5) * 0.8),
            petal
        );
        spin.setByAngle(rightSide ? -360 : 360);
        spin.setCycleCount(RotateTransition.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);

        new ParallelTransition(fall, spin).play();
    }

    private void positionSideNode(
            Label node,
            boolean rightSide,
            double inset,
            double nodeWidth) {

        if (rightSide) {
            node.layoutXProperty().bind(
                japaneseDecorLayer.widthProperty().subtract(inset + nodeWidth)
            );
        } else {
            node.setLayoutX(inset);
        }
    }
}
