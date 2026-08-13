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
import gp.loop.service.LocationFinanceService;
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

    @Test
    void locationReportCombinesFinanceAndInventoryData() throws Exception {
        try (var connection = Database.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS inventory_Warehouses ("
                    + "warehouseID TEXT PRIMARY KEY, name TEXT NOT NULL, "
                    + "serviceArea TEXT NOT NULL, addressAliases TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS inventory_Stock ("
                    + "stockYear INTEGER NOT NULL, stockCode TEXT NOT NULL, "
                    + "ingredientID INTEGER NOT NULL, stockQuantity INTEGER NOT NULL, "
                    + "warehouseID TEXT, capacity INTEGER NOT NULL, "
                    + "PRIMARY KEY(stockYear, stockCode))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS inventory_WarehouseDeliveries ("
                    + "deliveryID INTEGER PRIMARY KEY, warehouseID TEXT NOT NULL, "
                    + "ingredientID INTEGER NOT NULL, quantity INTEGER NOT NULL, "
                    + "status TEXT NOT NULL, expectedAt TEXT NOT NULL, "
                    + "createdAt TEXT, updatedAt TEXT)");
            statement.executeUpdate("INSERT OR IGNORE INTO inventory_Warehouses VALUES "
                    + "('WH-01','Central Kitchen Warehouse','Central London','london')");
            statement.executeUpdate("INSERT OR IGNORE INTO inventory_Stock VALUES "
                    + "(2026,'LOC-WH01-001',1,125,'WH-01',500)");
            statement.executeUpdate("INSERT OR IGNORE INTO inventory_WarehouseDeliveries VALUES "
                    + "(1,'WH-01',1,80,'Scheduled','2026-08-12 10:00:00',"
                    + "'2026-08-11 10:00:00','2026-08-11 10:00:00')");
        }

        List<gp.loop.model.LocationFinanceRow> rows =
                new LocationFinanceService().listLocationPerformance();
        assertEquals(1, rows.size());
        assertEquals(3, rows.get(0).getOrderCount());
        assertEquals(104.97, rows.get(0).getRevenue(), 1e-6);
        assertEquals(125, rows.get(0).getStockUnits());
        assertEquals(1, rows.get(0).getActiveDeliveries());
    }
}
