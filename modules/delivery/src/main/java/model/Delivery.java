package model;

public class Delivery {

    private String DeliveryId;
    private int OderId;
    private String Customer;
    private int CustomerId;
    private double Total;
    private String Status;
    private String Driver;
    private String Action;

    public Delivery(String deliveryId, int oderId,
            String customer, int customerId, double total, String status,
            String driver, String action) {
        this.DeliveryId = deliveryId;
        this.OderId = oderId;
        this.Customer = customer;
        this.CustomerId = customerId;
        this.Total = total;
        this.Status = status;
        this.Driver = driver;
        this.Action = action;
    }

    public String getDeliveryId() {
        return DeliveryId;
    }

    public int getOderId() {
        return OderId;
    }

    public String getCustomer() {
        return Customer;
    }

    public int getCustomerId() {
        return CustomerId;
    }

    public double getTotal() {
        return Total;
    }

    public String getStatus() {
        return Status;
    }

    public String getDriver() {
        return Driver;
    }

    public String getAction() {
        return Action;
    }
}