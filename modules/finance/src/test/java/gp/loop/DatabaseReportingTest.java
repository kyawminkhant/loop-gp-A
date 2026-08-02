package gp.loop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import gp.loop.db.Database;
import gp.loop.model.OrderRow;
import gp.loop.service.FinanceReporting;
import gp.loop.service.FinanceService;
import gp.loop.service.ReportingService;

/**
 * JUnit tests that run against a throw-away SQLite database (Requirements 1, 3, 4 and 5).
 * The {@code loop.db.path} system property redirects {@link Database} to a temp file so the
 * user's real DATA.db is never touched.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseReportingTest {

    static {
        try {
            Path tmp = Files.createTempFile("loop-test-", ".db");
            Files.deleteIfExists(tmp);
            System.setProperty("loop.db.path", tmp.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private final ReportingService reporting = new ReportingService();
    private final FinanceReporting financeReporting = new FinanceReporting();
    private final FinanceService finance = new FinanceService();

    @BeforeAll
    void setUp() throws Exception {
        Database.initialize(); // creates schema and seeds 3 demo orders
    }

    @Test
    void revenueAllTimeSumsSeededOrders() throws Exception {
        // Seed data: 45.99 + 45.99 + 12.99 = 104.97
        assertEquals(104.97, reporting.revenueAllTime(), 1e-6);
    }

    @Test
    void orderCountMatchesSeedData() throws Exception {
        assertEquals(3, financeReporting.getTotalSales());
    }

    @Test
    void profitAllTimeIsRevenueMinusCogs() throws Exception {
        double revenue = financeReporting.getIncomeSummaries();
        double cogs = financeReporting.calculateProductCost();
        assertEquals(revenue - cogs, reporting.profitAllTime(), 1e-6);
    }

    @Test
    void dateRangeFilterExcludesOrdersOutsideWindow() throws Exception {
        // Seeded orders are all in March 2026 — a January-only window must return zero
        double janRevenue = reporting.revenueBetween(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));
        assertEquals(0.0, janRevenue, 1e-6);

        double marchRevenue = reporting.revenueBetween(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1));
        assertEquals(104.97, marchRevenue, 1e-6);
    }

    @Test
    void monthlyMapAlwaysHasTwelveMonths() throws Exception {
        Map<String, Double> byMonth = reporting.revenueByMonth();
        assertEquals(12, byMonth.size());
        assertTrue(byMonth.containsKey("Jan"));
        assertTrue(byMonth.containsKey("Dec"));
        assertEquals(104.97, byMonth.get("Mar"), 1e-6);
    }

    @Test
    void listRecentOrdersReturnsRowsWithCosts() throws Exception {
        List<OrderRow> rows = finance.listRecentOrders(10);
        assertEquals(3, rows.size());
        assertFalse(rows.get(0).getProfitDisplay().isBlank());
    }

    @Test
    void csvExportWritesHeaderAndDataRows() throws Exception {
        Path out = Files.createTempFile("loop-report-", ".csv");
        financeReporting.filterReport(
                LocalDateTime.of(LocalDate.of(2026, 1, 1), LocalTime.MIN),
                LocalDateTime.of(LocalDate.of(2026, 12, 31), LocalTime.MAX),
                null, null, out);
        List<String> lines = Files.readAllLines(out);
        assertTrue(lines.size() >= 5, "expected comment, header and 3 data rows");
        assertTrue(lines.get(1).startsWith("OrderID,OrderDate,OrderTotal"));
        Files.deleteIfExists(out);
    }
}
