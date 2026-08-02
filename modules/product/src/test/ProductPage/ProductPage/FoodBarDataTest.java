package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class FoodBarDataTest {

    @Test
    void extendedDescriptionFallsBackToShortDescription() {
        FoodBarData data = sampleFood("", null);

        assertEquals("Short description", data.getExtendedDescription());
    }

    @Test
    void imageListUsesMainImageWhenNoListProvided() {
        FoodBarData data = sampleFood("Long description", null);

        assertEquals(List.of("main.png"), data.getProductImageLocations());
    }

    @Test
    void imageListReturnsDefensiveCopy() {
        FoodBarData data = sampleFood("Long description", List.of("one.png", "two.png"));

        data.getProductImageLocations().clear();

        assertEquals(2, data.getProductImageLocations().size());
    }

    private FoodBarData sampleFood(String extendedDescription, List<String> images) {
        return new FoodBarData(
            1,
            "Test Meal",
            "Short description",
            extendedDescription,
            9.99,
            2,
            4.5,
            12,
            "Vegetarian",
            "Balanced Meals",
            "Asian",
            "Japan",
            "2026-07-10",
            300,
            20,
            40,
            5,
            10,
            2,
            8,
            400,
            "main.png",
            images,
            "diet.png",
            "goal.png",
            "cuisine.png",
            null
        );
    }
}
