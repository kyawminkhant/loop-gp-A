package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;

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

    private void requireDatabase() {
        Assumptions.assumeTrue(
            Files.exists(Path.of("database", "loop.db")),
            "database/loop.db is required for read-only database tests"
        );
    }
}
