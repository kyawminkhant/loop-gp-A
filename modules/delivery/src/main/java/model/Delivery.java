package model;

public class Delivery {

    private final String deliveryId;
    private final int orderId;
    private final String customer;
    private final String customerId;
    private final double total;
    private final String status;
    private final String driver;
    private final String action;

    public Delivery(String deliveryId, int orderId,
            String customer, String customerId, double total, String status,
            String driver, String action) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customer = customer;
        this.customerId = customerId == null ? "Guest" : customerId;
        this.total = total;
        this.status = status;
        this.driver = driver == null || driver.isBlank() ? "Unassigned" : driver;
        this.action = action;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public String getDriver() {
        return driver;
    }

    public String getAction() {
        return action;
    }
}
