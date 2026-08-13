package ProductPage.ProductPage;

import java.util.ArrayList;
import java.util.List;

public class FoodBarData {

    private final int productId;
    private final String productName;
    private final String shortDescription;
    private final String extendedDescription;
    private final double price;
    private final int spiceLevel;
    private final double averageRating;
    private final int ratingCount;
    private final String dietary;
    private final String healthGoal;
    private final String cuisine;
    private final String country;
    private final String createdDate;
    private final double totalCalories;
    private final double totalProtein;
    private final double totalCarbohydrates;
    private final double totalSugars;
    private final double totalFat;
    private final double totalSaturatedFat;
    private final double totalFiber;
    private final double totalSodium;
    private final String productImageLocation;
    private final ArrayList<String> productImageLocations;
    private final String dietaryIconLocation;
    private final String goalIconLocation;
    private final String cuisineIconLocation;
    private final String actionImageLocation;
    private final boolean available;
    private final String availabilityMessage;

    public FoodBarData(
            int productId,
            String productName,
            String shortDescription,
            String extendedDescription,
            double price,
            int spiceLevel,
            double averageRating,
            int ratingCount,
            String dietary,
            String healthGoal,
            String cuisine,
            String country,
            String createdDate,
            double totalCalories,
            double totalProtein,
            double totalCarbohydrates,
            double totalSugars,
            double totalFat,
            double totalSaturatedFat,
            double totalFiber,
            double totalSodium,
            String productImageLocation,
            List<String> productImageLocations,
            String dietaryIconLocation,
            String goalIconLocation,
            String cuisineIconLocation,
            String actionImageLocation) {

        this(productId, productName, shortDescription, extendedDescription,
                price, spiceLevel, averageRating, ratingCount, dietary,
                healthGoal, cuisine, country, createdDate, totalCalories,
                totalProtein, totalCarbohydrates, totalSugars, totalFat,
                totalSaturatedFat, totalFiber, totalSodium,
                productImageLocation, productImageLocations,
                dietaryIconLocation, goalIconLocation, cuisineIconLocation,
                actionImageLocation, true, "");
    }

    public FoodBarData(
            int productId,
            String productName,
            String shortDescription,
            String extendedDescription,
            double price,
            int spiceLevel,
            double averageRating,
            int ratingCount,
            String dietary,
            String healthGoal,
            String cuisine,
            String country,
            String createdDate,
            double totalCalories,
            double totalProtein,
            double totalCarbohydrates,
            double totalSugars,
            double totalFat,
            double totalSaturatedFat,
            double totalFiber,
            double totalSodium,
            String productImageLocation,
            List<String> productImageLocations,
            String dietaryIconLocation,
            String goalIconLocation,
            String cuisineIconLocation,
            String actionImageLocation,
            boolean available,
            String availabilityMessage) {

        this.productId = productId;
        this.productName = productName;
        this.shortDescription = shortDescription;
        this.extendedDescription = extendedDescription;
        this.price = price;
        this.spiceLevel = spiceLevel;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.dietary = dietary;
        this.healthGoal = healthGoal;
        this.cuisine = cuisine;
        this.country = country;
        this.createdDate = createdDate;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbohydrates = totalCarbohydrates;
        this.totalSugars = totalSugars;
        this.totalFat = totalFat;
        this.totalSaturatedFat = totalSaturatedFat;
        this.totalFiber = totalFiber;
        this.totalSodium = totalSodium;
        this.productImageLocation = productImageLocation;
        this.productImageLocations = new ArrayList<>();
        if (productImageLocations != null) {
            this.productImageLocations.addAll(productImageLocations);
        }
        if (this.productImageLocations.isEmpty()
                && productImageLocation != null
                && !productImageLocation.trim().isEmpty()) {
            this.productImageLocations.add(productImageLocation);
        }
        this.dietaryIconLocation = dietaryIconLocation;
        this.goalIconLocation = goalIconLocation;
        this.cuisineIconLocation = cuisineIconLocation;
        this.actionImageLocation = actionImageLocation;
        this.available = available;
        this.availabilityMessage = availabilityMessage == null
                ? "" : availabilityMessage;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getShortDescription() { return shortDescription; }
    public String getExtendedDescription() {
        return extendedDescription == null || extendedDescription.trim().isEmpty()
            ? shortDescription
            : extendedDescription;
    }
    public double getPrice() { return price; }
    public int getSpiceLevel() { return spiceLevel; }
    public double getAverageRating() { return averageRating; }
    public int getRatingCount() { return ratingCount; }
    public String getDietary() { return dietary; }
    public String getHealthGoal() { return healthGoal; }
    public String getCuisine() { return cuisine; }
    public String getCountry() { return country; }
    public String getCreatedDate() { return createdDate; }
    public double getTotalCalories() { return totalCalories; }
    public double getTotalProtein() { return totalProtein; }
    public double getTotalCarbohydrates() { return totalCarbohydrates; }
    public double getTotalSugars() { return totalSugars; }
    public double getTotalFat() { return totalFat; }
    public double getTotalSaturatedFat() { return totalSaturatedFat; }
    public double getTotalFiber() { return totalFiber; }
    public double getTotalSodium() { return totalSodium; }
    public String getProductImageLocation() { return productImageLocation; }
    public ArrayList<String> getProductImageLocations() {
        return new ArrayList<>(productImageLocations);
    }
    public String getDietaryIconLocation() { return dietaryIconLocation; }
    public String getGoalIconLocation() { return goalIconLocation; }
    public String getCuisineIconLocation() { return cuisineIconLocation; }
    public String getActionImageLocation() { return actionImageLocation; }
    public boolean isAvailable() { return available; }
    public String getAvailabilityMessage() { return availabilityMessage; }
}
