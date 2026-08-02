package ProductPage.ProductPage;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartStoreTest {

    @BeforeEach
    void resetCart() {
        CartStore.clear();
    }

    @Test
    void addItemStoresProductInCart() {
        CartStore.addItem(1, 8.99, 2, false, "Base: Quinoa");

        assertEquals(2, CartStore.getTotalQuantity());
        assertEquals(1, CartStore.getCartItems().size());
        assertEquals(8.99, CartStore.getCartItems().get(0).getPrice());
    }

    @Test
    void sameProductAndDetailsMergesQuantity() {
        CartStore.addItem(1, 8.99, 1, false, "Base: Quinoa");
        CartStore.addItem(1, 8.99, 2, false, "Base: Quinoa");

        assertEquals(3, CartStore.getTotalQuantity());
        assertEquals(1, CartStore.getCartItems().size());
    }

    @Test
    void differentCustomisationCreatesSeparateCartItem() {
        CartStore.addItem(1, 8.99, 1, false, "Base: Quinoa");
        CartStore.addItem(1, 9.79, 1, false, "Base: Wild rice");

        assertEquals(2, CartStore.getTotalQuantity());
        assertEquals(2, CartStore.getCartItems().size());
    }

    @Test
    void zeroQuantityIsIgnored() {
        CartStore.addItem(1, 8.99, 0, false, "Base: Quinoa");

        assertEquals(0, CartStore.getTotalQuantity());
        assertTrue(CartStore.getCartItems().isEmpty());
    }

    @Test
    void weeklyAndOneTimeItemsStaySeparate() {
        CartStore.addItem(1, 8.99, 1, false, "Base: Quinoa");
        CartStore.addItem(1, 8.99, 1, true, "Base: Quinoa");

        assertEquals(2, CartStore.getTotalQuantity());
        assertEquals(2, CartStore.getCartItems().size());
        assertEquals(1, CartStore.getOneTimeProductIds().size());
        assertEquals(1, CartStore.getWeeklyProductIds().size());
    }
}
