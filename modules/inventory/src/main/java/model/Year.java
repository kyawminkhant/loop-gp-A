package model;

public class Year {

    private String ingredientID;
    private String ingredientName;
    private int stockQuantity;
    private String warehouseID;
    private int capacity;


    public Year(String ingredientID, 
                String ingredientName, 
                int stockQuantity,
                String warehouseID,
                int capacity) {

        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.stockQuantity = stockQuantity;
        this.warehouseID = warehouseID;
        this.capacity = capacity;
    }


    public String getIngredientID() {
        return ingredientID;
    }


    public String getIngredientName() {
        return ingredientName;
    }


    public int getStockQuantity() {
        return stockQuantity;
    }


    public String getWarehouseID() {
        return warehouseID;
    }


    public int getCapacity() {
        return capacity;
    }

}