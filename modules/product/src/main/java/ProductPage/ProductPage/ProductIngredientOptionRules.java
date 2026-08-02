package ProductPage.ProductPage;

import java.util.ArrayList;
import java.util.List;

final class ProductIngredientOptionRules {

    private ProductIngredientOptionRules() {
    }

    static List<Option> fallbackAddOns(String details) {
        String clean = normalise(details);
        List<Option> options = new ArrayList<>();

        if (containsAny(clean, "mediterranean", "greek", "salad", "feta", "olive")) {
            add(options, "Pickled red onion", 0.40);
            add(options, "Garlic yogurt sauce", 0.60);
            add(options, "Feta crumble", 0.80);
            add(options, "Fresh herbs", 0.20);
        } else if (containsAny(clean, "thai", "pad thai", "satay", "noodle", "peanut", "southeast asian")) {
            add(options, "Crushed peanuts", 0.30);
            add(options, "Tamarind sauce", 0.50);
            add(options, "Fresh coriander", 0.20);
            add(options, "Chilli flakes", 0.20);
        } else if (containsAny(clean, "curry", "katsu", "asian", "japanese")) {
            add(options, "Pickled ginger", 0.30);
            add(options, "Crispy onions", 0.40);
            add(options, "Sesame seeds", 0.20);
        } else if (containsAny(clean, "seafood", "salmon", "prawn", "fish", "paella", "pescatarian")) {
            add(options, "Lemon wedge", 0.20);
            add(options, "Herb dressing", 0.50);
            add(options, "Extra vegetables", 0.70);
        } else {
            add(options, "Spring onions", 0.20);
            add(options, "Sesame seeds", 0.20);
            add(options, "House sauce", 0.50);
        }

        return options;
    }

    static String normalise(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase().replace('-', ' ').trim();
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(normalise(needle))) {
                return true;
            }
        }
        return false;
    }

    private static void add(List<Option> options, String name, double extraPrice) {
        options.add(new Option(name, extraPrice));
    }

    static final class Option {
        final String name;
        final double extraPrice;

        Option(String name, double extraPrice) {
            this.name = name;
            this.extraPrice = extraPrice;
        }
    }
}
