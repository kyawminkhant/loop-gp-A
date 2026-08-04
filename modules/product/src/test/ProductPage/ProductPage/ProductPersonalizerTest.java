package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ProductPersonalizerTest {

    @Test
    void veganCustomerSeesVeganProductsFirst() {
        FoodBarData halal = food(1, "Halal Bowl", "Halal", "High Protein", 420, 45, 55);
        FoodBarData vegan = food(2, "Vegan Bowl", "Vegan", "Balanced Meals", 560, 24, 70);

        ArrayList<FoodBarData> ranked = ProductPersonalizer.rank(
            List.of(halal, vegan),
            Set.of("Vegan")
        );

        assertEquals("Vegan Bowl", ranked.get(0).getProductName());
    }

    @Test
    void lowCalorieAndKetoPreferencesUseStoredNutrition() {
        FoodBarData lighter = food(1, "Lighter", "Vegetarian", "Weight Loss", 320, 18, 48);
        FoodBarData lowerCarb = food(2, "Lower Carb", "Halal", "High Protein", 510, 42, 22);

        assertEquals(
            "Lighter",
            ProductPersonalizer.rank(
                List.of(lowerCarb, lighter),
                Set.of("Low-Calorie")
            ).get(0).getProductName()
        );
        assertEquals(
            "Lower Carb",
            ProductPersonalizer.rank(
                List.of(lighter, lowerCarb),
                Set.of("Keto")
            ).get(0).getProductName()
        );
    }

    @Test
    void highProteinAndDietaryMatchesAccumulatePerAccount() {
        FoodBarData pescatarian = food(
            1, "Salmon", "Pescatarian", "High Protein", 600, 45, 50
        );
        FoodBarData highProtein = food(
            2, "Chicken", "Halal", "High Protein", 500, 50, 45
        );

        ArrayList<FoodBarData> ranked = ProductPersonalizer.rank(
            List.of(highProtein, pescatarian),
            Set.of("Pescatarian", "High-Protein")
        );

        assertEquals("Salmon", ranked.get(0).getProductName());
    }

    @Test
    void noPreferencesPreserveTheDatabaseOrder() {
        FoodBarData first = food(1, "First", "Halal", "High Protein", 600, 50, 50);
        FoodBarData second = food(2, "Second", "Vegan", "Weight Loss", 300, 12, 35);

        ArrayList<FoodBarData> ranked = ProductPersonalizer.rank(
            List.of(first, second),
            Set.of()
        );

        assertEquals(List.of(first, second), ranked);
    }

    private FoodBarData food(
            int id,
            String name,
            String dietary,
            String healthGoal,
            double calories,
            double protein,
            double carbohydrates) {
        return new FoodBarData(
            id,
            name,
            name + " short description",
            name + " extended description",
            9.99,
            0,
            4.5,
            10,
            dietary,
            healthGoal,
            "Asian",
            "United Kingdom",
            "2026-08-04",
            calories,
            protein,
            carbohydrates,
            0,
            0,
            0,
            0,
            0,
            null,
            List.of(),
            null,
            null,
            null,
            null
        );
    }
}
