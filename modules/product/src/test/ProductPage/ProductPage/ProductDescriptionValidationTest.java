package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ProductDescriptionValidationTest {

    private static final Pattern WORD = Pattern.compile("\\b[\\w'-]+\\b");

    @Test
    void allStoredProductsPassDescriptionWordCounts() throws ClassNotFoundException, SQLException {
        requireDatabase();

        ArrayList<ArrayList<String>> products = DatabaseController.getData("Products");
        List<String> failures = new ArrayList<>();

        for (ArrayList<String> product : products) {
            String name = product.get(1);
            int shortWords = wordCount(product.get(2));
            int extendedWords = wordCount(product.get(3));

            if (shortWords < 30 || shortWords > 50 || extendedWords < 80) {
                failures.add(name + " short=" + shortWords + " extended=" + extendedWords);
            }
        }

        assertTrue(failures.isEmpty(), "Invalid product descriptions: " + failures);
    }

    private int wordCount(String value) {
        return value == null ? 0 : (int) WORD.matcher(value).results().count();
    }

    private void requireDatabase() {
        Assumptions.assumeTrue(
            Files.exists(Path.of("database", "loop.db")),
            "database/loop.db is required for description validation tests"
        );
    }
}
