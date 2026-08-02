package tests;

import database.CustomerDAO;
import database.DatabaseConnection;
import models.Customer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOTest {

    private static final String TEST_EMAIL = "junit.customer@example.com";
    private static final String TEST_ID = "JUNIT-ID-001";
    private static final String TEST_IMAGE = "id_cards/junit_test_id.png";

    private final CustomerDAO customerDAO = new CustomerDAO();

    @BeforeAll
    static void setupSuite() throws Exception {
        DatabaseConnection.useTestDatabase();
        new File("id_cards").mkdirs();
        Files.copy(
                new File("src/images/app_icon_64.png").toPath(),
                new File(TEST_IMAGE).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );
        DatabaseConnection.initializeDatabase();
    }

    @AfterAll
    static void tearDownSuite() {
        DatabaseConnection.useMainDatabase();
    }

    @Test
    void registerCustomer_insertsNewRecord() {
        if (customerDAO.emailExists(TEST_EMAIL)) {
            assertTrue(customerDAO.idCardExists(TEST_ID));
            return;
        }

        boolean registered = customerDAO.registerCustomer(
                "JUnit Customer",
                TEST_EMAIL,
                "07123456789",
                "secret123",
                "10 Test Street",
                TEST_ID,
                TEST_IMAGE
        );

        assertTrue(registered);
        assertTrue(customerDAO.emailExists(TEST_EMAIL));
    }

    @Test
    void authenticate_validCredentials_returnsCustomer() {
        Customer customer = customerDAO.authenticate(TEST_EMAIL, "secret123");
        assertNotNull(customer);
        assertEquals(TEST_EMAIL, customer.getEmail());
    }

    @Test
    void authenticate_wrongPassword_returnsNull() {
        assertNull(customerDAO.authenticate(TEST_EMAIL, "wrong-password"));
    }

    @Test
    void updateProfile_changesNameAndMobile() {
        Customer customer = customerDAO.authenticate(TEST_EMAIL, "secret123");
        assertNotNull(customer);

        boolean updated = customerDAO.updateProfile(
                customer.getCustomerID(),
                customer.getPersonID(),
                "Updated JUnit Name",
                "07987654321",
                "20 Updated Street"
        );

        assertTrue(updated);

        Customer refreshed = customerDAO.authenticate(TEST_EMAIL, "secret123");
        assertEquals("Updated JUnit Name", refreshed.getName());
        assertEquals("07987654321", refreshed.getMobile());
    }
}
