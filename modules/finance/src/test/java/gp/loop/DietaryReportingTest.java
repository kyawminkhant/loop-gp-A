package gp.loop;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.junit.jupiter.api.Test;

import gp.loop.db.Database;
import gp.loop.service.ReportingService;
import gp.loop.service.ReportingService.DietaryTotals;

/**
 * Checks the dietary breakdown, which joins this component's order data to the Product
 * component's {@code product_Category} table.
 *
 * <p>The other test classes redirect {@code loop.db.path} to a throw-away database that holds
 * only the finance tables, and all tests share one JVM. These checks therefore only apply when
 * the shared group database is in use, so they are skipped rather than failed otherwise.
 */
class DietaryReportingTest {

    private final ReportingService reporting = new ReportingService();

    private boolean sharedDatabaseInUse() throws Exception {
        try (Connection c = Database.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM sqlite_master WHERE name = 'product_Category'")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    @Test
    void dietaryBreakdownReturnsSegments() throws Exception {
        assumeTrue(sharedDatabaseInUse(), "shared group database not in use");

        Map<String, DietaryTotals> byDietary = reporting.revenueByDietary();
        assertFalse(byDietary.isEmpty(), "expected at least one dietary segment");

        for (Map.Entry<String, DietaryTotals> entry : byDietary.entrySet()) {
            assertFalse(entry.getKey() == null || entry.getKey().isBlank(),
                    "dietary name should never be blank");
            assertTrue(entry.getValue().getRevenue() >= 0, "revenue should not be negative");
            assertTrue(entry.getValue().getProfit() <= entry.getValue().getRevenue(),
                    "profit can never exceed revenue for " + entry.getKey());
        }
    }

    @Test
    void resultsAreOrderedByRevenueDescending() throws Exception {
        assumeTrue(sharedDatabaseInUse(), "shared group database not in use");

        Map<String, DietaryTotals> byDietary = reporting.revenueByDietary();
        double previous = Double.MAX_VALUE;
        for (DietaryTotals totals : byDietary.values()) {
            assertTrue(totals.getRevenue() <= previous, "segments should be highest revenue first");
            previous = totals.getRevenue();
        }
    }
}
