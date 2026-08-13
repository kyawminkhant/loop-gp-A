package orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void newOrderStatusIsNotFinal() {
        assertFalse(OrderStatus.PENDING.isFinal());
        assertFalse(OrderStatus.CONFIRMED.isFinal());
        assertFalse(OrderStatus.PREPARING.isFinal());
        assertFalse(OrderStatus.OUT_FOR_DELIVERY.isFinal());
    }

    @Test
    void deliveredAndCancelledAreFinal() {
        assertTrue(OrderStatus.DELIVERED.isFinal());
        assertTrue(OrderStatus.CANCELLED.isFinal());
    }

    @Test
    void fromDbValueMatchesByLabel() {
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.fromDbValue("Confirmed"));
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.fromDbValue("Out For Delivery"));
    }

    @Test
    void fromDbValueMatchesByEnumName() {
        assertEquals(OrderStatus.PREPARING, OrderStatus.fromDbValue("PREPARING"));
    }

    @Test
    void fromDbValueFallsBackToPendingForUnknownOrNullValues() {
        assertEquals(OrderStatus.PENDING, OrderStatus.fromDbValue("not-a-real-status"));
        assertEquals(OrderStatus.PENDING, OrderStatus.fromDbValue(null));
    }
}
