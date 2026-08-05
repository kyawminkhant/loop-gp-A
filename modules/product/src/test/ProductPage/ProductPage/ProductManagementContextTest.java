package ProductPage.ProductPage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductManagementContextTest {

    @AfterEach
    void clearContext() {
        ProductManagementContext.clearReturnToAdmin();
    }

    @Test
    void adminReturnRunsOnceAndClearsItself() {
        AtomicInteger returns = new AtomicInteger();
        ProductManagementContext.setReturnToAdmin(returns::incrementAndGet);

        assertTrue(ProductManagementContext.hasAdminReturn());
        assertTrue(ProductManagementContext.returnToAdmin());
        assertEquals(1, returns.get());
        assertFalse(ProductManagementContext.hasAdminReturn());
        assertFalse(ProductManagementContext.returnToAdmin());
    }
}
