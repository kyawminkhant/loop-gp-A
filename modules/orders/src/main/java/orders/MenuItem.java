package orders;


public final class MenuItem {
    public final int id;
    public final String name;
    public final double price;

    public MenuItem(int id, String name, double price) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.price = price;
    }
}
