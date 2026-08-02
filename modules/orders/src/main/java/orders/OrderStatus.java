package orders;


public enum OrderStatus {
    PENDING("Pending", "order-status-pending"),
    CONFIRMED("Confirmed", "order-status-confirmed"),
    PREPARING("Preparing", "order-status-preparing"),
    OUT_FOR_DELIVERY("Out For Delivery", "order-status-outfordelivery"),
    DELIVERED("Delivered", "order-status-delivered"),
    CANCELLED("Cancelled", "order-status-cancelled");

    private final String label;
    private final String styleClass;

    OrderStatus(String label, String styleClass) {
        this.label = label;
        this.styleClass = styleClass;
    }

    public String label() {
        return label;
    }

    public String styleClass() {
        return styleClass;
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }

    public static OrderStatus fromDbValue(String value) {
        if (value == null) {
            return PENDING;
        }
        for (OrderStatus status : values()) {
            if (status.label.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }

    @Override
    public String toString() {
        return label;
    }
}
