package orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void lineTotalMultipliesPriceByQuantity() {
        OrderLineItem line = new OrderLineItem(1, "Kimchi Fried Rice", 3, 9.99);
        assertEquals(29.97, line.lineTotal(), 0.001);
    }

    @Test
    void itemsSummaryListsEachLineWithQuantityAndName() {
        Order order = new Order(1, "Alex", "2026-08-13", OrderStatus.PENDING, 31.97, List.of(
            new OrderLineItem(1, "Chicken Shawarma Bowl", 2, 10.99),
            new OrderLineItem(2, "Kimchi Fried Rice", 1, 9.99)
        ));

        assertEquals("2x Chicken Shawarma Bowl, 1x Kimchi Fried Rice", order.itemsSummary());
    }

    @Test
    void itemsSummaryHandlesNoItems() {
        Order order = new Order(2, "Guest", "2026-08-13", OrderStatus.PENDING, 0, List.of());
        assertEquals("(no items)", order.itemsSummary());
    }

    @Test
    void blankCustomerNameIsKeptAsIs() {
        Order order = new Order(3, "   ", "2026-08-13", OrderStatus.PENDING, 0, List.of());
        assertEquals("   ", order.customerName);
    }

    @Test
    void nullCustomerNameDefaultsToGuest() {
        Order order = new Order(4, null, "2026-08-13", OrderStatus.PENDING, 0, List.of());
        assertEquals("Guest", order.customerName);
    }
}
