package orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CartStoreTest {

    @AfterEach
    void clearCartBetweenTests() {
        CartStore.clear();
    }

    @Test
    void newCartStartsEmpty() {
        assertTrue(CartStore.isEmpty());
        assertEquals(0, CartStore.getTotalQuantity());
        assertEquals(0, CartStore.getTotal(), 0.001);
    }

    @Test
    void addingAnItemUpdatesTotalsCorrectly() {
        MenuItem item = new MenuItem(1, "Thai Green Curry", 10.49);
        CartStore.addItem(item, 2);

        assertEquals(2, CartStore.getTotalQuantity());
        assertEquals(20.98, CartStore.getTotal(), 0.001);
    }

    @Test
    void addingTheSameItemTwiceMergesIntoOneLine() {
        MenuItem item = new MenuItem(1, "Miso Tofu Ramen", 9.49);
        CartStore.addItem(item, 1);
        CartStore.addItem(item, 2);

        assertEquals(1, CartStore.getLines().size());
        assertEquals(3, CartStore.getLines().get(0).getQuantity());
    }

    @Test
    void clearEmptiesTheCart() {
        CartStore.addItem(new MenuItem(1, "Margherita Flatbread", 8.49), 1);
        CartStore.clear();

        assertTrue(CartStore.isEmpty());
    }
}
