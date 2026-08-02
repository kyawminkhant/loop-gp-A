package gp.loop.model;

public final class Product {
    private final long productId;
    private final String name;
    private final double price;
    private final double cost;

    public Product(long productId, String name, double price, double cost) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.cost = cost;
    }

    public long getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getCost() { return cost; }
}
