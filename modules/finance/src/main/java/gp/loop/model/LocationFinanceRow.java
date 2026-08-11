package gp.loop.model;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Read-only finance summary for one Inventory warehouse and its UK service area.
 */
public final class LocationFinanceRow {

    private static final NumberFormat GBP = NumberFormat.getCurrencyInstance(Locale.UK);

    private final String warehouseId;
    private final String warehouseName;
    private final String serviceArea;
    private final int orderCount;
    private final double revenue;
    private final double cost;
    private final int stockUnits;
    private final int stockCapacity;
    private final int unavailableIngredients;
    private final int activeDeliveries;
    private final int inboundUnits;

    public LocationFinanceRow(
            String warehouseId,
            String warehouseName,
            String serviceArea,
            int orderCount,
            double revenue,
            double cost,
            int stockUnits,
            int stockCapacity,
            int unavailableIngredients,
            int activeDeliveries,
            int inboundUnits) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.serviceArea = serviceArea;
        this.orderCount = orderCount;
        this.revenue = revenue;
        this.cost = cost;
        this.stockUnits = stockUnits;
        this.stockCapacity = stockCapacity;
        this.unavailableIngredients = unavailableIngredients;
        this.activeDeliveries = activeDeliveries;
        this.inboundUnits = inboundUnits;
    }

    public String getWarehouseId() { return warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public String getServiceArea() { return serviceArea; }
    public int getOrderCount() { return orderCount; }
    public double getRevenue() { return revenue; }
    public double getCost() { return cost; }
    public double getProfit() { return revenue - cost; }
    public int getStockUnits() { return stockUnits; }
    public int getStockCapacity() { return stockCapacity; }
    public int getUnavailableIngredients() { return unavailableIngredients; }
    public int getActiveDeliveries() { return activeDeliveries; }
    public int getInboundUnits() { return inboundUnits; }

    public String getLocationDisplay() { return serviceArea + " (" + warehouseId + ")"; }
    public String getOrdersDisplay() { return Integer.toString(orderCount); }
    public String getRevenueDisplay() { return GBP.format(revenue); }
    public String getCostDisplay() { return GBP.format(cost); }
    public String getProfitDisplay() { return GBP.format(getProfit()); }
    public String getStockDisplay() {
        return String.format(Locale.UK, "%,d / %,d", stockUnits, stockCapacity);
    }
    public String getUnavailableDisplay() { return Integer.toString(unavailableIngredients); }
    public String getInboundDisplay() {
        return activeDeliveries == 0
                ? "None"
                : String.format(Locale.UK, "%d (%+,d units)", activeDeliveries, inboundUnits);
    }
}
