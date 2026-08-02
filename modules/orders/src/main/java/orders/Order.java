package orders;

import java.util.Collections;
import java.util.List;

public final class Order {
    public final int orderId;
    public final String customerName;
    public final String orderDate;
    public final OrderStatus status;
    public final double totalAmount;
    public final List<OrderLineItem> items;

    public Order(int orderId, String customerName, String orderDate,
            OrderStatus status, double totalAmount, List<OrderLineItem> items) {
        this.orderId = orderId;
        this.customerName = customerName == null ? "Guest" : customerName;
        this.orderDate = orderDate == null ? "" : orderDate;
        this.status = status == null ? OrderStatus.PENDING : status;
        this.totalAmount = totalAmount;
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    public String itemsSummary() {
        if (items.isEmpty()) {
            return "(no items)";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderLineItem item = items.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(item.quantity).append("x ").append(item.itemName);
        }
        return builder.toString();
    }
}
