package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ProductBrowseContextTest {

    @AfterEach
    void restoreStandardCatalogue() {
        ProductBrowseContext.useStandardCatalogue();
    }

    @Test
    void hubProductsCanForceTheStandardCatalogue() {
        ProductBrowseContext.usePersonalizedCatalogue();
        ProductBrowseContext.useStandardCatalogue();

        assertFalse(ProductBrowseContext.isPersonalized());
    }

    @Test
    void customerLoginCanSelectThePersonalizedCatalogue() {
        ProductBrowseContext.useStandardCatalogue();
        ProductBrowseContext.usePersonalizedCatalogue();

        assertTrue(ProductBrowseContext.isPersonalized());
    }
}
