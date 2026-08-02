package orders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public final class OrderRepository {

    private static final String DATABASE_URL = "jdbc:sqlite:" +
            System.getProperty("loop.db.path", "database/loop.db");

    private OrderRepository() {
    }

    
    public static void ensureSchema() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(
                "CREATE TABLE IF NOT EXISTS orders_Orders (" +
                "  orderID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  customerID TEXT," +
                "  customerName TEXT NOT NULL DEFAULT 'Guest'," +
                "  orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  status TEXT NOT NULL DEFAULT 'Pending'," +
                "  totalAmount REAL NOT NULL DEFAULT 0," +
                "  FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID)" +
                ")"
            );

            statement.execute(
                "CREATE TABLE IF NOT EXISTS orders_OrderItems (" +
                "  orderItemID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  orderID INTEGER NOT NULL," +
                "  productID INTEGER NOT NULL," +
                "  itemName TEXT NOT NULL," +
                "  quantity INTEGER NOT NULL DEFAULT 1," +
                "  priceAtOrder REAL NOT NULL DEFAULT 0," +
                "  FOREIGN KEY (orderID) REFERENCES orders_Orders(orderID)," +
                "  FOREIGN KEY (productID) REFERENCES product_Products(productID)" +
                ")"
            );
        }
    }

    public static List<MenuItem> getMenuItems() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT productID AS menuItemID, productName AS itemName, price "
                + "FROM product_Products WHERE status = 1 ORDER BY productName";
        List<MenuItem> items = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                items.add(new MenuItem(
                    result.getInt("menuItemID"),
                    result.getString("itemName"),
                    result.getDouble("price")
                ));
            }
        }
        return items;
    }

    
    public static int placeOrderFromCart(String customerName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        List<CartLine> cartLines = CartStore.getLines();
        if (cartLines.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        String resolvedName = (customerName == null || customerName.isBlank()) ? "Guest" : customerName.trim();
        double total = CartStore.getTotal();

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);

            try {
                int orderId = insertOrder(connection, resolvedName, total);

                for (CartLine line : cartLines) {
                    insertOrderItem(connection, orderId, line);
                }

                connection.commit();
                CartStore.clear();
                return orderId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static List<Order> getAllOrders() throws ClassNotFoundException, SQLException {
        return queryOrders(-1);
    }

    public static Order getOrderById(int orderId) throws ClassNotFoundException, SQLException {
        List<Order> results = queryOrders(orderId);
        return results.isEmpty() ? null : results.get(0);
    }

    public static void updateStatus(int orderId, OrderStatus newStatus) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "UPDATE orders_Orders SET status = ? WHERE orderID = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus.label());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    /**
     * Cancels an order, unless it has already been delivered or cancelled.
     *
     * @return true if the order was cancelled, false if it could not be.
     */
    public static boolean cancelOrder(int orderId) throws ClassNotFoundException, SQLException {
        Order existing = getOrderById(orderId);
        if (existing == null || existing.status.isFinal()) {
            return false;
        }
        updateStatus(orderId, OrderStatus.CANCELLED);
        return true;
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private static int insertOrder(Connection connection, String customerName, double total) throws SQLException {
        String sql = "INSERT INTO orders_Orders (customerID, customerName, status, totalAmount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, findCustomerId(connection, customerName));
            statement.setString(2, customerName);
            statement.setString(3, OrderStatus.PENDING.label());
            statement.setDouble(4, total);
            statement.executeUpdate();
        }

        // SQLite JDBC does not reliably support getGeneratedKeys() on a
        // PreparedStatement, so read back the row ID the standard SQLite way.
        try (Statement idStatement = connection.createStatement();
             ResultSet keys = idStatement.executeQuery("SELECT last_insert_rowid()")) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to obtain generated order ID.");
    }

    private static void insertOrderItem(Connection connection, int orderId, CartLine line) throws SQLException {
        String sql = "INSERT INTO orders_OrderItems (orderID, productID, itemName, quantity, priceAtOrder) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, line.menuItemId);
            statement.setString(3, line.itemName);
            statement.setInt(4, line.getQuantity());
            statement.setDouble(5, line.price);
            statement.executeUpdate();
        }
    }

    private static List<Order> queryOrders(int orderId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT orderID, customerName, orderDate, status, totalAmount FROM orders_Orders"
            + (orderId > 0 ? " WHERE orderID = ?" : "")
            + " ORDER BY orderDate DESC, orderID DESC";

        List<Order> orders = new ArrayList<>();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (orderId > 0) {
                statement.setInt(1, orderId);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int id = result.getInt("orderID");
                    orders.add(new Order(
                        id,
                        result.getString("customerName"),
                        result.getString("orderDate"),
                        OrderStatus.fromDbValue(result.getString("status")),
                        result.getDouble("totalAmount"),
                        loadItems(connection, id)
                    ));
                }
            }
        }
        return orders;
    }

    private static List<OrderLineItem> loadItems(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT productID AS menuItemID, itemName, quantity, priceAtOrder FROM orders_OrderItems "
            + "WHERE orderID = ? ORDER BY orderItemID";

        List<OrderLineItem> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new OrderLineItem(
                        result.getInt("menuItemID"),
                        result.getString("itemName"),
                        result.getInt("quantity"),
                        result.getDouble("priceAtOrder")
                    ));
                }
            }
        }
        return items;
    }

    private static String findCustomerId(Connection connection, String customerName) throws SQLException {
        String sql = "SELECT customer.customerID FROM customer_Customers customer "
                + "JOIN customer_People person ON person.personID = customer.personID "
                + "WHERE lower(trim(person.name)) = lower(trim(?)) LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    private static Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}
