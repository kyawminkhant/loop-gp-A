package model;

public class DeliveryDetails {

    private String deliveryId;
    private String customerName;
    private String customerAddress;

    public DeliveryDetails(String deliveryId, String customerName, String customerAddress) {
        this.deliveryId = deliveryId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }
}