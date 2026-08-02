package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ProductIngredientOptionRulesTest {

    @Test
    void mediterraneanDetailsReturnMediterraneanAddOns() {
        List<ProductIngredientOptionRules.Option> options =
            ProductIngredientOptionRules.fallbackAddOns("Greek Mediterranean salad");

        assertFalse(options.isEmpty());
        assertTrue(options.stream().anyMatch(option -> option.name.equals("Feta crumble")));
        assertTrue(options.stream().anyMatch(option -> option.extraPrice > 0));
    }

    @Test
    void thaiDetailsReturnThaiAddOns() {
        List<ProductIngredientOptionRules.Option> options =
            ProductIngredientOptionRules.fallbackAddOns("Pad Thai noodles");

        assertTrue(options.stream().anyMatch(option -> option.name.equals("Crushed peanuts")));
        assertTrue(options.stream().anyMatch(option -> option.name.equals("Tamarind sauce")));
    }

    @Test
    void unknownDetailsReturnHouseDefaults() {
        List<ProductIngredientOptionRules.Option> options =
            ProductIngredientOptionRules.fallbackAddOns("Unknown meal");

        assertEquals(3, options.size());
        assertEquals("Spring onions", options.get(0).name);
        assertEquals("House sauce", options.get(2).name);
    }

    @Test
    void normaliseHandlesNullAndHyphens() {
        assertEquals("", ProductIngredientOptionRules.normalise(null));
        assertEquals("gluten free", ProductIngredientOptionRules.normalise("Gluten-Free"));
    }
}
