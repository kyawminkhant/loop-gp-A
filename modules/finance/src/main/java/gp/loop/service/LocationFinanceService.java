package gp.loop.service;

import gp.loop.db.Database;
import gp.loop.model.LocationFinanceRow;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Builds finance figures from the warehouse locations owned by Inventory. */
public final class LocationFinanceService {

    public List<LocationFinanceRow> listLocationPerformance() throws Exception {
        try (Connection connection = Database.getConnection()) {
            if (!tableExists(connection, "inventory_Warehouses")) {
                return List.of();
            }

            Map<String, LocationTotals> totals = loadWarehouses(connection);
            loadStock(connection, totals);
            loadDeliveries(connection, totals);
            loadOrders(connection, totals);

            List<LocationFinanceRow> rows = new ArrayList<>();
            for (LocationTotals total : totals.values()) {
                rows.add(total.toRow());
            }
            return rows;
        }
    }

    public Path exportCsv(Path destination) throws Exception {
        Path absolute = destination.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(
                absolute, StandardCharsets.UTF_8)) {
            writer.write("WarehouseID,Warehouse,ServiceArea,Orders,Revenue,Cost,Profit,"
                    + "StockUnits,StockCapacity,UnavailableIngredients,ActiveDeliveries,InboundUnits");
            writer.newLine();
            for (LocationFinanceRow row : listLocationPerformance()) {
                writer.write(csv(row.getWarehouseId()) + ","
                        + csv(row.getWarehouseName()) + ","
                        + csv(row.getServiceArea()) + ","
                        + row.getOrderCount() + ","
                        + decimal(row.getRevenue()) + ","
                        + decimal(row.getCost()) + ","
                        + decimal(row.getProfit()) + ","
                        + row.getStockUnits() + ","
                        + row.getStockCapacity() + ","
                        + row.getUnavailableIngredients() + ","
                        + row.getActiveDeliveries() + ","
                        + row.getInboundUnits());
                writer.newLine();
            }
        }
        return absolute;
    }

    private Map<String, LocationTotals> loadWarehouses(Connection connection)
            throws Exception {
        Map<String, LocationTotals> totals = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT warehouseID, name, serviceArea, addressAliases
                     FROM inventory_Warehouses ORDER BY warehouseID
                     """)) {
            while (result.next()) {
                LocationTotals total = new LocationTotals(
                        result.getString("warehouseID"),
                        result.getString("name"),
                        result.getString("serviceArea"),
                        result.getString("addressAliases"));
                totals.put(total.warehouseId, total);
            }
        }
        return totals;
    }

    private void loadStock(Connection connection, Map<String, LocationTotals> totals)
            throws Exception {
        if (!tableExists(connection, "inventory_Stock")) {
            return;
        }
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT warehouseID,
                            SUM(stockQuantity) AS stockUnits,
                            SUM(capacity) AS stockCapacity,
                            SUM(CASE WHEN stockQuantity <= 0 THEN 1 ELSE 0 END)
                              AS unavailableIngredients
                     FROM inventory_Stock
                     WHERE stockYear = (SELECT MAX(stockYear) FROM inventory_Stock)
                     GROUP BY warehouseID
                     """)) {
            while (result.next()) {
                LocationTotals total = totals.get(result.getString("warehouseID"));
                if (total != null) {
                    total.stockUnits = result.getInt("stockUnits");
                    total.stockCapacity = result.getInt("stockCapacity");
                    total.unavailableIngredients = result.getInt("unavailableIngredients");
                }
            }
        }
    }

    private void loadDeliveries(Connection connection, Map<String, LocationTotals> totals)
            throws Exception {
        if (!tableExists(connection, "inventory_WarehouseDeliveries")) {
            return;
        }
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT warehouseID,
                            COUNT(*) AS activeDeliveries,
                            SUM(quantity) AS inboundUnits
                     FROM inventory_WarehouseDeliveries
                     WHERE status IN ('Scheduled', 'In Transit')
                     GROUP BY warehouseID
                     """)) {
            while (result.next()) {
                LocationTotals total = totals.get(result.getString("warehouseID"));
                if (total != null) {
                    total.activeDeliveries = result.getInt("activeDeliveries");
                    total.inboundUnits = result.getInt("inboundUnits");
                }
            }
        }
    }

    private void loadOrders(Connection connection, Map<String, LocationTotals> totals)
            throws Exception {
        if (totals.isEmpty()
                || !tableExists(connection, "orders_Orders")
                || !tableExists(connection, "orders_OrderItems")
                || !tableExists(connection, "product_Products")) {
            return;
        }

        boolean hasCustomers = tableExists(connection, "customer_Customers");
        String sql = "SELECT o.orderID, o.totalAmount, "
                + (hasCustomers ? "c.deliveryAddress" : "NULL AS deliveryAddress")
                + ", IFNULL(SUM(IFNULL(p.cost, 0) * IFNULL(item.quantity, 0)), 0) AS orderCost "
                + "FROM orders_Orders o "
                + "LEFT JOIN orders_OrderItems item ON item.orderID = o.orderID "
                + "LEFT JOIN product_Products p ON p.productID = item.productID "
                + (hasCustomers
                    ? "LEFT JOIN customer_Customers c ON c.customerID = o.customerID "
                    : "")
                + "GROUP BY o.orderID, o.totalAmount"
                + (hasCustomers ? ", c.deliveryAddress " : " ")
                + "ORDER BY o.orderID";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                LocationTotals total = resolveLocation(
                        totals, result.getString("deliveryAddress"));
                if (total != null) {
                    total.orderCount++;
                    total.revenue += result.getDouble("totalAmount");
                    total.cost += result.getDouble("orderCost");
                }
            }
        }
    }

    private LocationTotals resolveLocation(
            Map<String, LocationTotals> totals, String deliveryAddress) {
        LocationTotals fallback = totals.values().iterator().next();
        String address = normalise(deliveryAddress);
        if (address.isEmpty()) {
            return fallback;
        }
        for (LocationTotals total : totals.values()) {
            for (String alias : total.addressAliases.split(",")) {
                String normalisedAlias = normalise(alias);
                if (!normalisedAlias.isEmpty() && address.contains(normalisedAlias)) {
                    return total;
                }
            }
        }
        return fallback;
    }

    private boolean tableExists(Connection connection, String tableName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private static final class LocationTotals {
        private final String warehouseId;
        private final String warehouseName;
        private final String serviceArea;
        private final String addressAliases;
        private int orderCount;
        private double revenue;
        private double cost;
        private int stockUnits;
        private int stockCapacity;
        private int unavailableIngredients;
        private int activeDeliveries;
        private int inboundUnits;

        private LocationTotals(
                String warehouseId,
                String warehouseName,
                String serviceArea,
                String addressAliases) {
            this.warehouseId = warehouseId;
            this.warehouseName = warehouseName;
            this.serviceArea = serviceArea;
            this.addressAliases = addressAliases == null ? "" : addressAliases;
        }

        private LocationFinanceRow toRow() {
            return new LocationFinanceRow(
                    warehouseId, warehouseName, serviceArea, orderCount, revenue, cost,
                    stockUnits, stockCapacity, unavailableIngredients,
                    activeDeliveries, inboundUnits);
        }
    }
}
