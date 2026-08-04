package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import models.Customer;
import models.CustomerPreference;

class CustomerPreferenceProfileTest {

    @Test
    void profileUsesCustomerNameAndCanonicalSavedPreferences() {
        Customer customer = new Customer();
        customer.setName("Priya Sharma");

        CustomerPreference preference = new CustomerPreference();
        preference.setFavoriteCategories(
            "Vegan,low calorie,High Protein,Pescatarian"
        );

        CustomerPreferenceProfile profile =
            CustomerPreferenceProfile.from(customer, preference);

        assertTrue(profile.isSignedIn());
        assertEquals("Priya", profile.getFirstName());
        assertEquals(
            Set.of("Vegan", "Low-Calorie", "High-Protein", "Pescatarian"),
            profile.getPreferences()
        );
        assertTrue(profile.getRecommendationMessage().contains("Vegan"));
    }

    @Test
    void generalPreferenceKeepsAValidSignedInProfileWithoutFoodFilters() {
        Customer customer = new Customer();
        customer.setName("Demo Customer");

        CustomerPreference preference = new CustomerPreference();
        preference.setFavoriteCategories("General");

        CustomerPreferenceProfile profile =
            CustomerPreferenceProfile.from(customer, preference);

        assertTrue(profile.isSignedIn());
        assertTrue(profile.getPreferences().isEmpty());
        assertTrue(profile.getRecommendationMessage().contains("add food preferences"));
    }

    @Test
    void guestProfileDoesNotPretendAUserIsSignedIn() {
        CustomerPreferenceProfile profile = CustomerPreferenceProfile.guest();

        assertFalse(profile.isSignedIn());
        assertEquals("Guest", profile.getFirstName());
        assertTrue(profile.getPreferences().isEmpty());
    }
}
