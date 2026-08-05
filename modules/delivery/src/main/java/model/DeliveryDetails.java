package model;

public class DeliveryDetails {

    private final String deliveryId;
    private final int orderId;
    private final String customerName;
    private final String customerAddress;
    private final String status;
    private final String driver;

    public DeliveryDetails(String deliveryId, int orderId, String customerName,
            String customerAddress, String status, String driver) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerAddress = customerAddress == null ? "Address unavailable" : customerAddress;
        this.status = status;
        this.driver = driver == null || driver.isBlank() ? "Unassigned" : driver;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getStatus() {
        return status;
    }

    public String getDriver() {
        return driver;
    }
}
