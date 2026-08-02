package orders;


public final class CartLine {
    public final int menuItemId;
    public final String itemName;
    public final double price;
    private int quantity;

    public CartLine(int menuItemId, String itemName, double price, int quantity) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    void addQuantity(int amount) {
        quantity += amount;
    }

    public double lineTotal() {
        return price * quantity;
    }
}
