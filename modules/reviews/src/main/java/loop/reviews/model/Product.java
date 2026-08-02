package loop.reviews.model;

/**
 * Product entity (maps to "products"). averageRating is auto-updated whenever a
 * review is submitted, edited or removed (FR9).
 */
public class Product {
    private int id;
    private String name;
    private double price;      // must be > 0
    private int stock;         // must be >= 0
    private String category;
    private double averageRating;

    public Product() { }

    public Product(int id, String name, double price, int stock, String category, double averageRating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.averageRating = averageRating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    @Override
    public String toString() { return name; }
}
