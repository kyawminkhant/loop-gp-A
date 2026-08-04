package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class DatabaseReadTest {

    @Test
    void productsTableCanBeRead() throws ClassNotFoundException, SQLException {
        requireDatabase();

        ArrayList<ArrayList<String>> products = DatabaseController.getData("Products");

        assertFalse(products.isEmpty());
        assertTrue(products.get(0).size() >= 12);
    }

    @Test
    void categoryTableCanBeRead() throws ClassNotFoundException, SQLException {
        requireDatabase();

        ArrayList<ArrayList<String>> categories = DatabaseController.getData("Category");

        assertFalse(categories.isEmpty());
        assertTrue(categories.get(0).size() >= 6);
    }

    @Test
    void ingredientTableCanBeRead() throws ClassNotFoundException, SQLException {
        requireDatabase();

        ArrayList<ArrayList<String>> ingredients = DatabaseController.getData("Ingredient");

        assertFalse(ingredients.isEmpty());
        assertTrue(ingredients.get(0).size() >= 10);
    }

    @Test
    void activeProductCardsCanBeLoadedFromSharedTables()
            throws ClassNotFoundException, SQLException {
        requireDatabase();

        List<FoodBarData> products = FoodBarRepository.loadActiveProducts();

        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(product -> product.getProductId() > 0));
        assertTrue(products.stream().allMatch(product -> !product.getProductName().isBlank()));
    }

    private void requireDatabase() {
        Path databasePath = Path.of(
            System.getProperty("loop.db.path", "database/loop.db")
        );
        Assumptions.assumeTrue(
            Files.exists(databasePath),
            databasePath + " is required for read-only database tests"
        );
    }
}
