package ProductPage.ProductPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Places products matching the signed-in customer's preferences first. */
final class ProductPersonalizer {

    private ProductPersonalizer() { }

    static ArrayList<FoodBarData> rank(
            List<FoodBarData> products,
            Set<String> preferences) {
        ArrayList<FoodBarData> ranked = new ArrayList<>(products);
        if (preferences == null || preferences.isEmpty()) {
            return ranked;
        }

        ranked.sort(Comparator.comparingInt(
            (FoodBarData product) -> preferenceScore(product, preferences)
        ).reversed());
        return ranked;
    }

    static int preferenceScore(FoodBarData product, Set<String> preferences) {
        int score = 0;
        for (String preference : preferences) {
            String key = normalise(preference);
            switch (key) {
                case "vegan":
                    score += tagMatches(product.getDietary(), "vegan") ? 1200 : 0;
                    break;
                case "vegetarian":
                    score += (tagMatches(product.getDietary(), "vegetarian")
                            || tagMatches(product.getDietary(), "vegan")) ? 1200 : 0;
                    break;
                case "halal":
                case "pescatarian":
                case "glutenfree":
                    score += tagMatches(product.getDietary(), key) ? 1200 : 0;
                    break;
                case "highprotein":
                    score += tagMatches(product.getHealthGoal(), "highprotein")
                        ? 1200
                        : nutritionScore(product.getTotalProtein(), 10);
                    break;
                case "weightloss":
                    score += tagMatches(product.getHealthGoal(), "weightloss")
                        ? 1200
                        : lowValueScore(product.getTotalCalories(), 800, 1);
                    break;
                case "lowcalorie":
                    score += lowValueScore(product.getTotalCalories(), 800, 1);
                    break;
                case "keto":
                    score += lowValueScore(product.getTotalCarbohydrates(), 150, 2);
                    break;
                default:
                    if (tagMatches(product.getDietary(), key)
                            || tagMatches(product.getHealthGoal(), key)
                            || tagMatches(product.getCuisine(), key)
                            || tagMatches(product.getCountry(), key)) {
                        score += 1000;
                    }
                    break;
            }
        }
        return score;
    }

    private static int nutritionScore(double value, int multiplier) {
        return value <= 0 ? 0 : (int) Math.round(value * multiplier);
    }

    private static int lowValueScore(double value, int ceiling, int multiplier) {
        if (value <= 0) {
            return 0;
        }
        return Math.max(0, ceiling - (int) Math.round(value * multiplier));
    }

    private static boolean tagMatches(String actual, String expected) {
        String cleanExpected = normalise(expected);
        return !cleanExpected.isBlank() && normalise(actual).contains(cleanExpected);
    }

    private static String normalise(String value) {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
