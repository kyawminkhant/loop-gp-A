package ProductPage.ProductPage;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductAvailabilityServiceTest {

    @Test
    void availabilityFollowsRequiredIngredientStock() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE product_Ingredient ("
                    + "ingredientID INTEGER PRIMARY KEY, ingredientName TEXT NOT NULL)");
            statement.execute("CREATE TABLE inventory_Stock ("
                    + "stockYear INTEGER, ingredientID INTEGER, stockQuantity INTEGER, "
                    + "warehouseID TEXT)");
            statement.execute("CREATE TABLE product_DefaultIngredient ("
                    + "productID INTEGER, defaultIngredients TEXT NOT NULL)");

            statement.execute("INSERT INTO product_Ingredient VALUES (1, 'Rice'), (2, 'Tomato')");
            statement.execute("INSERT INTO inventory_Stock VALUES "
                    + "(2026, 1, 5, 'WH-01'), (2026, 2, 0, 'WH-01')");
            statement.execute("INSERT INTO product_DefaultIngredient VALUES "
                    + "(10, '1,0.00 | 2,0.00 | '), (11, '1,0.00 | ')");

            Map<Integer, ProductAvailabilityService.Availability> availability =
                    ProductAvailabilityService.loadAll(connection, 2026);

            assertFalse(availability.get(10).isAvailable());
            assertEquals(java.util.List.of("Tomato"),
                    availability.get(10).getMissingIngredients());
            assertTrue(availability.get(11).isAvailable());

            statement.execute("UPDATE inventory_Stock SET stockQuantity = 4 "
                    + "WHERE ingredientID = 2");
            availability = ProductAvailabilityService.loadAll(connection, 2026);
            assertTrue(availability.get(10).isAvailable());
        }
    }

    @Test
    void customerLocationUsesOnlyItsServingWarehouse() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE product_Ingredient ("
                    + "ingredientID INTEGER PRIMARY KEY, ingredientName TEXT NOT NULL)");
            statement.execute("CREATE TABLE inventory_Stock ("
                    + "stockYear INTEGER, ingredientID INTEGER, stockQuantity INTEGER, "
                    + "warehouseID TEXT)");
            statement.execute("CREATE TABLE product_DefaultIngredient ("
                    + "productID INTEGER, defaultIngredients TEXT NOT NULL)");
            statement.execute("INSERT INTO product_Ingredient VALUES "
                    + "(1, 'Rice'), (2, 'Tomato')");
            statement.execute("INSERT INTO inventory_Stock VALUES "
                    + "(2026, 1, 5, 'WH-01'), (2026, 2, 5, 'WH-01'), "
                    + "(2026, 1, 5, 'WH-02'), (2026, 2, 0, 'WH-02')");
            statement.execute("INSERT INTO product_DefaultIngredient VALUES "
                    + "(10, '1,0.00 | 2,0.00 | ')");

            Map<Integer, ProductAvailabilityService.Availability> central =
                    ProductAvailabilityService.loadAll(
                            connection, 2026, "WH-01", "WH-01 - Central London");
            Map<Integer, ProductAvailabilityService.Availability> kingston =
                    ProductAvailabilityService.loadAll(
                            connection, 2026, "WH-02", "WH-02 - Kingston");

            assertTrue(central.get(10).isAvailable());
            assertFalse(kingston.get(10).isAvailable());
            assertTrue(kingston.get(10).getUnavailableMessage().contains("Kingston"));
            assertTrue(kingston.get(10).getUnavailableMessage().contains("Tomato"));
        }
    }

    @Test
    void parsesLegacyDefaultIngredientFormat() {
        assertEquals(java.util.List.of(4, 11, 31),
                ProductAvailabilityService.parseIngredientIds(
                        "4,0.00 | 11,1.25 | bad | 31,0.00 | "));
    }
}
