package loop.reviews.model;

/**
 * Bridging entity from the Order Management component (maps to "orders").
 * Used by verifyPurchase() to confirm a customer bought a product before a
 * review can be accepted (FR2 / FR3).
 */
public class Order {
    private int id;
    private int customerId;
    private int productId;
    private String orderDate;

    public Order() { }

    public Order(int id, int customerId, int productId, String orderDate) {
        this.id = id;
        this.customerId = customerId;
        this.productId = productId;
        this.orderDate = orderDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
}
