package gp.loop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gp.loop.service.FinanceReporting;

/** JUnit tests for the pure calculation logic in {@link FinanceReporting} (Requirements 1 and 2). */
class FinanceReportingTest {

    private final FinanceReporting reporting = new FinanceReporting();

    @Test
    void profitMarginNormalCase() {
        // profit 50 on cost 100 = 50% margin vs cost
        assertEquals(50.0, reporting.calculateProfitMargin(50.0, 100.0), 1e-9);
    }

    @Test
    void profitMarginZeroCostWithProfitIsHundredPercent() {
        assertEquals(100.0, reporting.calculateProfitMargin(25.0, 0.0), 1e-9);
    }

    @Test
    void profitMarginZeroCostZeroProfitIsZero() {
        assertEquals(0.0, reporting.calculateProfitMargin(0.0, 0.0), 1e-9);
    }

    @Test
    void profitMarginNegativeProfitGivesNegativeMargin() {
        // losing 20 on a cost of 80 = -25%
        assertEquals(-25.0, reporting.calculateProfitMargin(-20.0, 80.0), 1e-9);
    }
}
