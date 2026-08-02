package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

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
        Assumptions.assumeTrue(
            Files.exists(Path.of("database", "loop.db")),
            "database/loop.db is required for read-only repository tests"
        );
    }
}
