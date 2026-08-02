package gp.loop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import gp.loop.model.OrderRow;

/** JUnit tests for {@link OrderRow} date parsing and profit display (Requirements 2 and 4). */
class OrderRowTest {

    @Test
    void parsesIsoDateTime() {
        OrderRow row = new OrderRow(1, "2026-03-03T10:00:00", 45.99, 27.25);
        Optional<LocalDate> date = row.getOrderDateLocal();
        assertTrue(date.isPresent());
        assertEquals(LocalDate.of(2026, 3, 3), date.get());
    }

    @Test
    void parsesSqliteSpaceDateTime() {
        OrderRow row = new OrderRow(2, "2026-03-10 18:22:00", 12.99, 6.00);
        Optional<LocalDate> date = row.getOrderDateLocal();
        assertTrue(date.isPresent());
        assertEquals(LocalDate.of(2026, 3, 10), date.get());
    }

    @Test
    void parsesDateOnly() {
        OrderRow row = new OrderRow(3, "2026-07-01", 19.99, 11.00);
        Optional<LocalDate> date = row.getOrderDateLocal();
        assertTrue(date.isPresent());
        assertEquals(LocalDate.of(2026, 7, 1), date.get());
    }

    @Test
    void invalidDateReturnsEmptyOptional() {
        OrderRow row = new OrderRow(4, "not-a-date", 10.0, 5.0);
        assertTrue(row.getOrderDateLocal().isEmpty());
    }

    @Test
    void profitDisplayIsRevenueMinusCost() {
        OrderRow row = new OrderRow(5, "2026-07-01", 45.99, 27.25);
        // 45.99 - 27.25 = 18.74
        assertTrue(row.getProfitDisplay().contains("18.74"));
    }

    @Test
    void blankDateDisplaysDash() {
        OrderRow row = new OrderRow(6, "", 10.0, 5.0);
        assertEquals("—", row.getOrderDateDisplay());
    }
}
