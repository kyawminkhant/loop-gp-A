package ProductPage.ProductPage;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import database.CustomerDAO;
import models.Customer;
import models.CustomerPreference;
import utils.SessionManager;

/** Account identity and food preferences shared by Customer and Product. */
final class CustomerPreferenceProfile {

    private final boolean signedIn;
    private final String firstName;
    private final Set<String> preferences;

    private CustomerPreferenceProfile(
            boolean signedIn,
            String firstName,
            Set<String> preferences) {
        this.signedIn = signedIn;
        this.firstName = firstName;
        this.preferences = Collections.unmodifiableSet(new LinkedHashSet<>(preferences));
    }

    static CustomerPreferenceProfile current() {
        Customer customer = SessionManager.getCurrentCustomer();
        if (customer == null) {
            return guest();
        }

        CustomerPreference preference = new CustomerDAO().getPreference(customer.getCustomerID());
        return from(customer, preference);
    }

    static CustomerPreferenceProfile from(
            Customer customer,
            CustomerPreference preference) {
        if (customer == null) {
            return guest();
        }

        String saved = preference == null ? "" : preference.getFavoriteCategories();
        Set<String> parsed = new LinkedHashSet<>();
        if (saved != null) {
            for (String value : saved.split(",")) {
                String canonical = canonicalPreference(value);
                if (!canonical.isBlank() && !"General".equals(canonical)) {
                    parsed.add(canonical);
                }
            }
        }
        return new CustomerPreferenceProfile(true, firstName(customer.getName()), parsed);
    }

    static CustomerPreferenceProfile guest() {
        return new CustomerPreferenceProfile(false, "Guest", Collections.emptySet());
    }

    boolean isSignedIn() {
        return signedIn;
    }

    String getFirstName() {
        return firstName;
    }

    Set<String> getPreferences() {
        return preferences;
    }

    String getRecommendationMessage() {
        if (!signedIn) {
            return "Sign in through Customers to personalise your recommendations.";
        }
        if (preferences.isEmpty()) {
            return "Personalised for " + firstName
                + " - add food preferences in your Customer account.";
        }
        return "Recommended first for: " + preferences.stream()
            .collect(Collectors.joining("  |  "));
    }

    private static String firstName(String name) {
        if (name == null || name.isBlank()) {
            return "Customer";
        }
        String clean = name.trim().split("\\s+")[0];
        return clean.length() > 12 ? clean.substring(0, 12) + "..." : clean;
    }

    private static String canonicalPreference(String value) {
        String clean = value == null ? "" : value.trim();
        String key = clean.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        switch (key) {
            case "general": return "General";
            case "vegan": return "Vegan";
            case "vegetarian": return "Vegetarian";
            case "keto": return "Keto";
            case "glutenfree": return "Gluten-Free";
            case "halal": return "Halal";
            case "lowcalorie": return "Low-Calorie";
            case "pescatarian": return "Pescatarian";
            case "highprotein": return "High-Protein";
            case "weightloss": return "Weight-Loss";
            default: return clean;
        }
    }
}
