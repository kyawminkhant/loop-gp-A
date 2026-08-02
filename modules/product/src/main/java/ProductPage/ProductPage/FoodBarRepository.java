package ProductPage.ProductPage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FoodBarRepository {

    private static final String DEFAULT_DIETARY = "Not specified";
    private static final String DEFAULT_GOAL = "Balanced Meals";

    private FoodBarRepository() {
    }

    public static List<FoodBarData> loadActiveProducts()
            throws ClassNotFoundException, SQLException {

        ArrayList<ArrayList<String>> products =
            DatabaseController.getData("product_Products");
        ArrayList<ArrayList<String>> images =
            DatabaseController.getData("product_ProductImage");
        ArrayList<ArrayList<String>> categories =
            DatabaseController.getData("product_Category");
        ArrayList<ArrayList<String>> ratings =
            DatabaseController.getData("product_Ratings");
        ArrayList<ArrayList<String>> defaultIngredients =
            DatabaseController.getData("product_DefaultIngredient");

        Map<Integer, String> firstImageByProduct = mapFirstImages(images);
        Map<Integer, ArrayList<String>> imagesByProduct =
            mapImageLocations(images);
        Map<Integer, ArrayList<String>> categoryByProduct =
            mapRowsByProductId(categories, 5);
        Map<Integer, RatingSummary> ratingByProduct = mapRatings(ratings);
        Map<Integer, ArrayList<String>> nutritionByProduct =
            mapRowsByProductId(defaultIngredients, 10);

        List<FoodBarData> cards = new ArrayList<>();

        for (ArrayList<String> product : products) {
            // Products index order comes directly from DatabaseController.getData().
            int productId = parseInt(product, 0, 0);
            String status = value(product, 6, "Inactive");

            if (!"Active".equalsIgnoreCase(status)) {
                continue;
            }

            String name = value(product, 1, "Unnamed product");
            String shortDescription = value(product, 2, "");
            String extendedDescription = value(product, 3, shortDescription);
            double price = parseDouble(product, 5, 0);
            int spiceLevel = parseInt(product, 7, 0);
            String country = value(product, 8, "Not specified");
            String createdDate = value(product, 9, "");

            ArrayList<String> category = categoryByProduct.get(productId);
            String dietary = category == null
                    ? DEFAULT_DIETARY
                    : value(category, 2, DEFAULT_DIETARY);
            String healthGoal = category == null
                    ? DEFAULT_GOAL
                    : value(category, 3, DEFAULT_GOAL);
            String cuisine = category == null
                    ? country
                    : value(category, 4, country);
            RatingSummary rating = ratingByProduct.getOrDefault(
                productId,
                new RatingSummary(0, 0)
            );
            ArrayList<String> nutrition = nutritionByProduct.get(productId);

            cards.add(new FoodBarData(
                productId,
                name,
                shortDescription,
                extendedDescription,
                price,
                spiceLevel,
                rating.average,
                rating.count,
                dietary,
                healthGoal,
                cuisine,
                country,
                createdDate,
                parseDouble(nutrition, 2, 0),
                parseDouble(nutrition, 3, 0),
                parseDouble(nutrition, 4, 0),
                parseDouble(nutrition, 5, 0),
                parseDouble(nutrition, 6, 0),
                parseDouble(nutrition, 7, 0),
                parseDouble(nutrition, 8, 0),
                parseDouble(nutrition, 9, 0),
                firstImageByProduct.get(productId),
                imagesByProduct.get(productId),
                dietaryIcon(dietary),
                healthGoalIcon(healthGoal),
                cuisineIcon(cuisine),
                null
            ));
        }

        return cards;
    }

    private static Map<Integer, RatingSummary> mapRatings(
            ArrayList<ArrayList<String>> ratingRows) {

        Map<Integer, Double> weightedTotals = new HashMap<>();
        Map<Integer, Integer> peopleTotals = new HashMap<>();

        for (ArrayList<String> row : ratingRows) {
            // Ratings order: rateID, rating, noPeople, productID.
            int noPeople = Math.max(0, parseInt(row, 2, 0));
            int productId = parseInt(row, 3, 0);
            double rating = Math.max(0, Math.min(parseDouble(row, 1, 0), 5));

            weightedTotals.merge(productId, rating * noPeople, Double::sum);
            peopleTotals.merge(productId, noPeople, Integer::sum);
        }

        Map<Integer, RatingSummary> summaries = new HashMap<>();
        weightedTotals.forEach((productId, weightedTotal) -> {
            int noPeople = peopleTotals.getOrDefault(productId, 0);
            double average = noPeople == 0 ? 0 : weightedTotal / noPeople;
            summaries.put(productId, new RatingSummary(average, noPeople));
        });

        return summaries;
    }

    private static final class RatingSummary {
        private final double average;
        private final int count;

        private RatingSummary(double average, int count) {
            this.average = average;
            this.count = count;
        }
    }

    private static Map<Integer, String> mapFirstImages(
            ArrayList<ArrayList<String>> imageRows) {

        Map<Integer, ArrayList<String>> firstRows = new HashMap<>();

        imageRows.stream()
            .sorted(Comparator.comparingInt(row -> parseInt(row, 3, 0)))
            .forEach(row -> {
                int productId = parseInt(row, 5, 0);
                firstRows.putIfAbsent(productId, row);
            });

        Map<Integer, String> locations = new HashMap<>();
        firstRows.forEach((productId, row) ->
            locations.put(productId, value(row, 1, null))
        );
        return locations;
    }

    private static Map<Integer, ArrayList<String>> mapImageLocations(
            ArrayList<ArrayList<String>> imageRows) {

        Map<Integer, ArrayList<String>> locations = new HashMap<>();

        imageRows.stream()
            .sorted(Comparator.comparingInt(row -> parseInt(row, 3, 0)))
            .forEach(row -> {
                int productId = parseInt(row, 5, 0);
                String location = value(row, 1, null);

                if (location != null) {
                    locations
                        .computeIfAbsent(productId, key -> new ArrayList<>())
                        .add(location);
                }
            });

        return locations;
    }

    private static Map<Integer, ArrayList<String>> mapRowsByProductId(
            ArrayList<ArrayList<String>> rows,
            int productIdIndex) {

        Map<Integer, ArrayList<String>> result = new HashMap<>();
        for (ArrayList<String> row : rows) {
            result.put(parseInt(row, productIdIndex, 0), row);
        }
        return result;
    }

    private static String dietaryIcon(String dietary) {
        String value = normalise(dietary);

        if (value.contains("vegetarian")) {
            return "/ProductPage/ProductPage/FilterBox/vegetarian.png";
        }
        if (value.contains("vegan")) {
            return "/ProductPage/ProductPage/FilterBox/vegan.png";
        }
        if (value.contains("halal")) {
            return "/ProductPage/ProductPage/FilterBox/halal.png";
        }
        if (value.contains("gluten")) {
            return "/ProductPage/ProductPage/FilterBox/gluten-free.png";
        }
        if (value.contains("pescatarian")) {
        	return "/ProductPage/ProductPage/FilterBox/pescatarian.png";
        }
        return "/ProductPage/ProductPage/images/dietary.png";
    }

    private static String healthGoalIcon(String goal) {
        String value = normalise(goal);

        if (value.contains("weight")) {
            return "/ProductPage/ProductPage/FilterBox/weight-loss.png";
        }
        if (value.contains("protein")) {
            return "/ProductPage/ProductPage/FilterBox/high-protein.png";
        }
        if (value.contains("balanced")) {
            return "/ProductPage/ProductPage/FilterBox/balanced-meals.png";
        }
        return "/ProductPage/ProductPage/images/health.png";
    }

    private static String cuisineIcon(String cuisine) {
        String value = normalise(cuisine);

        if (value.contains("southeast")) {
            return "/ProductPage/ProductPage/FilterBox/southeast-asian.png";
        }
        if (value.contains("south asian")) {
            return "/ProductPage/ProductPage/FilterBox/south-asian.png";
        }
        if (value.contains("middle east")) {
            return "/ProductPage/ProductPage/FilterBox/middle-eastern.png";
        }
        if (value.contains("mediterranean")) {
            return "/ProductPage/ProductPage/FilterBox/mediterranean.png";
        }
        if (value.contains("western")) {
            return "/ProductPage/ProductPage/FilterBox/western.png";
        }
        if (value.contains("asia")
                || value.contains("korea")
                || value.contains("china")
                || value.contains("japan")) {
            return "/ProductPage/ProductPage/FilterBox/asian.png";
        }
        return "/ProductPage/ProductPage/images/cuisine.png";
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String value(
            ArrayList<String> row,
            int index,
            String fallback) {

        if (row == null || index < 0 || index >= row.size()) {
            return fallback;
        }

        String value = row.get(index);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static int parseInt(
            ArrayList<String> row,
            int index,
            int fallback) {

        try {
            return Integer.parseInt(value(row, index, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(
            ArrayList<String> row,
            int index,
            double fallback) {

        try {
            return Double.parseDouble(value(row, index, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
