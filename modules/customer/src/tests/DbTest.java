package tests;

import database.ChefReviewDAO;
import database.CustomerDAO;
import database.DatabaseConnection;
import models.Chef;
import models.Customer;
import utils.PasswordUtil;
import utils.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Manual test harness for Assessment B report (Testing section).
 * Run: DbTest.java -> Run As -> Java Application
 */
public class DbTest {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("=== LOOP CUSTOMERS — DATABASE & VALIDATION TESTS ===\n");
        System.out.printf("%-4s %-28s %-10s %-30s%n", "ID", "Component", "Result", "Notes");
        System.out.println("-".repeat(78));

        runTest(1, "Database connection", testDatabaseConnection());
        runTest(2, "Schema initialisation", testSchemaInit());
        runTest(3, "Password hashing", testPasswordHashing());
        runTest(4, "Email validation", testEmailValidation());
        runTest(5, "Mobile validation", testMobileValidation());
        runTest(6, "Customer registration", testRegistration());
        runTest(7, "Duplicate email check", testDuplicateEmail());
        runTest(8, "Login (valid password)", testValidLogin());
        runTest(9, "Login (wrong password)", testInvalidLogin());
        runTest(10, "Order history (dummy)", testOrderHistory());
        runTest(11, "Chef list (integration)", testChefList());
        runTest(12, "Chef review insert", testChefReview());

        System.out.println("-".repeat(78));
        System.out.println("PASSED: " + passCount + "  |  FAILED: " + failCount);
        System.out.println("=== TESTS FINISHED ===");
    }

    private static void runTest(int id, String component, TestResult result) {
        String status = result.passed ? "PASS" : "FAIL";
        if (result.passed) passCount++;
        else failCount++;
        System.out.printf("%-4d %-28s %-10s %-30s%n", id, component, status, result.note);
    }

    private static TestResult testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return conn != null && !conn.isClosed()
                    ? TestResult.pass("SQLite JDBC connected")
                    : TestResult.fail("Connection null/closed");
        } catch (SQLException e) {
            return TestResult.fail(e.getMessage());
        }
    }

    private static TestResult testSchemaInit() {
        try {
            DatabaseConnection.initializeDatabase();
            return TestResult.pass("Tables created / verified");
        } catch (Exception e) {
            return TestResult.fail(e.getMessage());
        }
    }

    private static TestResult testPasswordHashing() {
        String hash = PasswordUtil.hash("secret123");
        boolean ok = hash != null && !hash.equals("secret123");
        return ok ? TestResult.pass("Plain text not stored") : TestResult.fail("Hash equals plain text");
    }

    private static TestResult testEmailValidation() {
        return ValidationUtil.isValidEmail("student@brunel.ac.uk")
                ? TestResult.pass("Valid email accepted")
                : TestResult.fail("Valid email rejected");
    }

    private static TestResult testMobileValidation() {
        return !ValidationUtil.isValidMobile("12345")
                ? TestResult.pass("Short mobile rejected")
                : TestResult.fail("Invalid mobile accepted");
    }

    private static final String TEST_EMAIL = "assessment.test@example.com";
    private static final String TEST_ID = "ASSESS-ID-001";

    private static TestResult testRegistration() {
        CustomerDAO dao = new CustomerDAO();
        if (dao.emailExists(TEST_EMAIL) || dao.idCardExists(TEST_ID)) {
            return TestResult.pass("Skipped — test user already exists");
        }
        boolean ok = dao.registerCustomer(
                "Assessment Test User", TEST_EMAIL, "07123456789",
                "secret123", "10 Brunel Road", TEST_ID, "id_cards/test_id.png");
        return ok ? TestResult.pass("New customer inserted") : TestResult.fail("Register returned false");
    }

    private static TestResult testDuplicateEmail() {
        CustomerDAO dao = new CustomerDAO();
        return dao.emailExists(TEST_EMAIL)
                ? TestResult.pass("Duplicate detected")
                : TestResult.fail("Duplicate not detected");
    }

    private static TestResult testValidLogin() {
        CustomerDAO dao = new CustomerDAO();
        Customer c = dao.authenticate(TEST_EMAIL, "secret123");
        return c != null ? TestResult.pass("Authenticated OK") : TestResult.fail("Login failed");
    }

    private static TestResult testInvalidLogin() {
        CustomerDAO dao = new CustomerDAO();
        Customer c = dao.authenticate(TEST_EMAIL, "wrongpass");
        return c == null ? TestResult.pass("Wrong password rejected") : TestResult.fail("Wrong password accepted");
    }

    private static TestResult testOrderHistory() {
        CustomerDAO dao = new CustomerDAO();
        Customer c = dao.authenticate(TEST_EMAIL, "secret123");
        if (c == null) return TestResult.fail("No logged-in customer");
        boolean ok = !dao.getOrderHistory(c.getCustomerID()).isEmpty();
        return ok ? TestResult.pass("Dummy orders present") : TestResult.fail("No orders found");
    }

    private static TestResult testChefList() {
        List<Chef> chefs = new ChefReviewDAO().getChefs();
        return !chefs.isEmpty() ? TestResult.pass(chefs.size() + " chefs loaded") : TestResult.fail("Chef list empty");
    }

    private static TestResult testChefReview() {
        CustomerDAO customerDAO = new CustomerDAO();
        ChefReviewDAO reviewDAO = new ChefReviewDAO();
        Customer c = customerDAO.authenticate(TEST_EMAIL, "secret123");
        if (c == null) return TestResult.fail("No customer for review");

        List<Chef> chefs = reviewDAO.getChefs();
        if (chefs.isEmpty()) return TestResult.fail("No chefs");

        if (reviewDAO.hasReviewed(c.getCustomerID(), chefs.get(0).getChefID())) {
            return TestResult.pass("Skipped — review already exists");
        }

        boolean ok = reviewDAO.addReview(c.getCustomerID(), chefs.get(0).getChefID(), 5,
                "Excellent chef with healthy and tasty meals");
        return ok ? TestResult.pass("Review inserted") : TestResult.fail("Review insert failed");
    }

    private record TestResult(boolean passed, String note) {
        static TestResult pass(String note) { return new TestResult(true, note); }
        static TestResult fail(String note) { return new TestResult(false, note); }
    }
}
