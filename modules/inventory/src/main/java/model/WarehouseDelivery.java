package model;

/** One inbound ingredient delivery for a LOOP warehouse. */
public final class WarehouseDelivery {

    private final int deliveryID;
    private final String warehouseID;
    private final String warehouseName;
    private final String serviceArea;
    private final int ingredientID;
    private final String ingredientName;
    private final int quantity;
    private final String status;
    private final String expectedAt;
    private final String updatedAt;

    public WarehouseDelivery(
            int deliveryID,
            String warehouseID,
            String warehouseName,
            String serviceArea,
            int ingredientID,
            String ingredientName,
            int quantity,
            String status,
            String expectedAt,
            String updatedAt) {
        this.deliveryID = deliveryID;
        this.warehouseID = warehouseID;
        this.warehouseName = warehouseName;
        this.serviceArea = serviceArea;
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.status = status;
        this.expectedAt = expectedAt;
        this.updatedAt = updatedAt;
    }

    public int getDeliveryID() { return deliveryID; }
    public String getWarehouseID() { return warehouseID; }
    public String getWarehouseName() { return warehouseName; }
    public String getServiceArea() { return serviceArea; }
    public int getIngredientID() { return ingredientID; }
    public String getIngredientName() { return ingredientName; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public String getExpectedAt() { return expectedAt; }
    public String getUpdatedAt() { return updatedAt; }
}
