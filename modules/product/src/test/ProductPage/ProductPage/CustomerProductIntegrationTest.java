package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import models.Customer;
import utils.SessionManager;

class CustomerProductIntegrationTest {

    @Test
    void currentCustomerSessionLoadsItsPreferenceRecordForProducts() {
        Path databasePath = Path.of(
            System.getProperty("loop.db.path", "database/loop.db")
        );
        Assumptions.assumeTrue(
            Files.exists(databasePath),
            databasePath + " is required for the Customer/Product integration test"
        );

        Customer customer = new Customer();
        customer.setCustomerID("demo-customer-001");
        customer.setName("Demo Customer");
        SessionManager.setCurrentCustomer(customer);

        try {
            CustomerPreferenceProfile profile = CustomerPreferenceProfile.current();

            assertTrue(profile.isSignedIn());
            assertEquals("Demo", profile.getFirstName());
        } finally {
            SessionManager.clear();
        }
    }
}
