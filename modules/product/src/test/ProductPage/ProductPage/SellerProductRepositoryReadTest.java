package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class SellerProductRepositoryReadTest {

    @Test
    void loadProductSummariesReturnsStoredProducts() throws ClassNotFoundException, SQLException {
        requireDatabase();

        List<SellerProductRepository.ProductSummary> products =
            SellerProductRepository.loadProductSummaries();

        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(product -> product.id > 0));
        assertTrue(products.stream().allMatch(product -> product.price > 0));
        assertTrue(products.stream().allMatch(product ->
                product.active == (product.manuallyActive && product.inventoryAvailable)));
        assertEquals(productOwnedIds(), products.stream()
            .map(product -> product.id)
            .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void loadChefsReturnsChefRecords() throws ClassNotFoundException, SQLException {
        requireDatabase();

        List<SellerProductRepository.ChefRecord> chefs =
            SellerProductRepository.loadChefs();

        assertFalse(chefs.isEmpty());
        assertTrue(chefs.stream().allMatch(chef -> chef.id > 0));
        assertTrue(chefs.stream().allMatch(chef -> !chef.name.isBlank()));
    }

    @Test
    void loadIngredientsReturnsNutritionRecords() throws ClassNotFoundException, SQLException {
        requireDatabase();

        List<SellerProductRepository.IngredientRecord> ingredients =
            SellerProductRepository.loadIngredients();

        assertFalse(ingredients.isEmpty());
        assertTrue(ingredients.stream().allMatch(ingredient -> ingredient.id > 0));
        assertTrue(ingredients.stream().allMatch(ingredient -> !ingredient.name.isBlank()));
    }

    @Test
    void loadProductForEditReturnsImagesAndEditableFields() throws ClassNotFoundException, SQLException {
        requireDatabase();

        int productId = SellerProductRepository.loadProductSummaries().get(0).id;
        SellerProductRepository.ProductEditRecord product =
            SellerProductRepository.loadProductForEdit(productId);

        assertEquals(productId, product.id);
        assertFalse(product.productName.isBlank());
        assertTrue(product.price > 0);
        assertTrue(product.cost >= 0);
        assertFalse(product.imageUrls.isEmpty());
    }

    private void requireDatabase() {
        Path databasePath = databasePath();
        Assumptions.assumeTrue(
            Files.exists(databasePath),
            databasePath + " is required for read-only repository tests"
        );
    }

    private Set<Integer> productOwnedIds() throws SQLException {
        Set<Integer> ids = new HashSet<>();
        String url = "jdbc:sqlite:" + databasePath();
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT productID FROM product_Products WHERE sourceModule = 'product'");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                ids.add(result.getInt("productID"));
            }
        }
        return ids;
    }

    private Path databasePath() {
        return Path.of(System.getProperty("loop.db.path", "database/loop.db"));
    }
}
