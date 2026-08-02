package orders;


public final class OrderLineItem {
    public final int menuItemId;
    public final String itemName;
    public final int quantity;
    public final double priceAtOrder;

    public OrderLineItem(int menuItemId, String itemName, int quantity, double priceAtOrder) {
        this.menuItemId = menuItemId;
        this.itemName = itemName == null ? "" : itemName;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public double lineTotal() {
        return priceAtOrder * quantity;
    }
}
