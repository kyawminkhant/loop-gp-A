package DAO;

import Database.DBconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Delivery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Delivery operations backed by the same orders_Orders rows as Orders. */
public final class DeliveryDAO {

    private DeliveryDAO() { }

    public static void ensureSchemaAndSync() throws SQLException {
        try (Connection connection = DBconnection.getConnection()) {
            ensureSchemaAndSync(connection);
        }
    }

    public static ObservableList<Delivery> getDelivery() {
        return queryDeliveries("");
    }

    public static ObservableList<Delivery> getUnassignedDeliveries() {
        return queryDeliveries(
                "WHERE (delivery.driver IS NULL OR trim(delivery.driver) = '') "
                + "AND orders.status NOT IN ('Delivered', 'Cancelled')");
    }

    public static ObservableList<Delivery> getActiveDeliveries() {
        return queryDeliveries(
                "WHERE delivery.driver IS NOT NULL AND trim(delivery.driver) <> '' "
                + "AND orders.status NOT IN ('Delivered', 'Cancelled')");
    }

    public static ObservableList<Delivery> getActiveDeliveriesForDriver(String driverName) {
        ObservableList<Delivery> matches = FXCollections.observableArrayList();
        if (driverName == null || driverName.isBlank()) {
            return matches;
        }
        for (Delivery delivery : getActiveDeliveries()) {
            if (driverName.trim().equalsIgnoreCase(delivery.getDriver())) {
                matches.add(delivery);
            }
        }
        return matches;
    }

    public static boolean assignDriver(int orderId, String driverName) {
        if (driverName == null || driverName.isBlank()) {
            return false;
        }
        return updateDeliveryAndOrder(orderId, driverName.trim(), "Out For Delivery");
    }

    public static boolean markDelivered(int orderId) {
        return updateDeliveryAndOrder(orderId, null, "Delivered");
    }

    public static int[] getSummaryCounts() {
        String sql = "SELECT "
                + "SUM(CASE WHEN orders.status NOT IN ('Delivered', 'Cancelled') THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN (delivery.driver IS NULL OR trim(delivery.driver) = '') "
                + "AND orders.status NOT IN ('Delivered', 'Cancelled') THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN orders.status = 'Cancelled' THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN orders.status IN ('Pending', 'Confirmed', 'Preparing') THEN 1 ELSE 0 END) "
                + "FROM delivery_Deliveries delivery "
                + "JOIN orders_Orders orders ON orders.orderID = delivery.orderID";
        try (Connection connection = DBconnection.getConnection()) {
            ensureSchemaAndSync(connection);
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                if (result.next()) {
                    return new int[] {
                        result.getInt(1), result.getInt(2),
                        result.getInt(3), result.getInt(4)
                    };
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return new int[] {0, 0, 0, 0};
    }

    private static ObservableList<Delivery> queryDeliveries(String whereClause) {
        ObservableList<Delivery> deliveries = FXCollections.observableArrayList();
        String sql = "SELECT delivery.deliveryID, orders.orderID, orders.customerID, "
                + "orders.customerName, orders.totalAmount, orders.status, delivery.driver "
                + "FROM delivery_Deliveries delivery "
                + "JOIN orders_Orders orders ON orders.orderID = delivery.orderID "
                + whereClause + " ORDER BY orders.orderDate DESC, orders.orderID DESC";

        try (Connection connection = DBconnection.getConnection()) {
            ensureSchemaAndSync(connection);
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                while (result.next()) {
                    String status = result.getString("status");
                    String driver = result.getString("driver");
                    deliveries.add(new Delivery(
                            result.getString("deliveryID"),
                            result.getInt("orderID"),
                            result.getString("customerName"),
                            result.getString("customerID"),
                            result.getDouble("totalAmount"),
                            status,
                            driver,
                            actionFor(status, driver)
                    ));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return deliveries;
    }

    private static boolean updateDeliveryAndOrder(
            int orderId,
            String driverName,
            String status) {
        String updateDelivery = driverName == null
                ? "UPDATE delivery_Deliveries SET status = ?, updatedAt = CURRENT_TIMESTAMP "
                    + "WHERE orderID = ? AND EXISTS (SELECT 1 FROM orders_Orders orders "
                    + "WHERE orders.orderID = delivery_Deliveries.orderID "
                    + "AND orders.status NOT IN ('Delivered', 'Cancelled'))"
                : "UPDATE delivery_Deliveries SET driver = ?, status = ?, updatedAt = CURRENT_TIMESTAMP "
                    + "WHERE orderID = ? AND EXISTS (SELECT 1 FROM orders_Orders orders "
                    + "WHERE orders.orderID = delivery_Deliveries.orderID "
                    + "AND orders.status NOT IN ('Delivered', 'Cancelled'))";
        String updateOrder = "UPDATE orders_Orders SET status = ? WHERE orderID = ? "
                + "AND status NOT IN ('Delivered', 'Cancelled')";

        try (Connection connection = DBconnection.getConnection()) {
            ensureSchemaAndSync(connection);
            connection.setAutoCommit(false);
            try {
                int deliveryRows;
                try (PreparedStatement statement = connection.prepareStatement(updateDelivery)) {
                    int parameter = 1;
                    if (driverName != null) {
                        statement.setString(parameter++, driverName);
                    }
                    statement.setString(parameter++, status);
                    statement.setInt(parameter, orderId);
                    deliveryRows = statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(updateOrder)) {
                    statement.setString(1, status);
                    statement.setInt(2, orderId);
                    statement.executeUpdate();
                }
                connection.commit();
                return deliveryRows > 0;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private static void ensureSchemaAndSync(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS orders_Orders ("
                    + "orderID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "customerID TEXT, customerName TEXT NOT NULL DEFAULT 'Guest', "
                    + "orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "status TEXT NOT NULL DEFAULT 'Pending', "
                    + "totalAmount REAL NOT NULL DEFAULT 0)");
            statement.execute("CREATE TABLE IF NOT EXISTS delivery_Deliveries ("
                    + "deliveryID TEXT PRIMARY KEY, "
                    + "orderID INTEGER NOT NULL UNIQUE, "
                    + "driver TEXT, "
                    + "status TEXT NOT NULL DEFAULT 'Pending', "
                    + "updatedAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (orderID) REFERENCES orders_Orders(orderID))");
            statement.execute("CREATE TRIGGER IF NOT EXISTS delivery_AfterOrderInsert "
                    + "AFTER INSERT ON orders_Orders BEGIN "
                    + "INSERT OR IGNORE INTO delivery_Deliveries "
                    + "(deliveryID, orderID, status) "
                    + "VALUES (printf('DEL-%04d', NEW.orderID), NEW.orderID, NEW.status); END");
            statement.execute("CREATE TRIGGER IF NOT EXISTS delivery_AfterOrderStatusUpdate "
                    + "AFTER UPDATE OF status ON orders_Orders BEGIN "
                    + "UPDATE delivery_Deliveries SET status = NEW.status, "
                    + "updatedAt = CURRENT_TIMESTAMP WHERE orderID = NEW.orderID; END");
            statement.executeUpdate("INSERT OR IGNORE INTO delivery_Deliveries "
                    + "(deliveryID, orderID, status) "
                    + "SELECT printf('DEL-%04d', orderID), orderID, status FROM orders_Orders");
            statement.executeUpdate("UPDATE delivery_Deliveries SET status = ("
                    + "SELECT orders.status FROM orders_Orders orders "
                    + "WHERE orders.orderID = delivery_Deliveries.orderID) "
                    + "WHERE EXISTS (SELECT 1 FROM orders_Orders orders "
                    + "WHERE orders.orderID = delivery_Deliveries.orderID "
                    + "AND orders.status <> delivery_Deliveries.status)");
            statement.executeUpdate("UPDATE delivery_Deliveries SET driver = CASE abs(orderID) % 5 "
                    + "WHEN 0 THEN 'Iman' WHEN 1 THEN 'Efrin' WHEN 2 THEN 'Prakash' "
                    + "WHEN 3 THEN 'Jonny' ELSE 'Samira' END "
                    + "WHERE (driver IS NULL OR trim(driver) = '') "
                    + "AND status IN ('Out For Delivery', 'Delivered')");
        }
    }

    private static String actionFor(String status, String driver) {
        if ("Cancelled".equalsIgnoreCase(status)) {
            return "None";
        }
        if ("Delivered".equalsIgnoreCase(status)) {
            return "Completed";
        }
        if (driver == null || driver.isBlank()) {
            return "Assign";
        }
        return "Track";
    }
}
