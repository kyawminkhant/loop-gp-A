package DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DeliveryDAOTest {

    private static Path databasePath;

    @BeforeAll
    static void createOrders() throws Exception {
        databasePath = Path.of(System.getProperty("loop.db.path"));
        Files.createDirectories(databasePath.getParent());
        Files.deleteIfExists(databasePath);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE orders_Orders ("
                    + "orderID INTEGER PRIMARY KEY AUTOINCREMENT, customerID TEXT, "
                    + "customerName TEXT NOT NULL, orderDate TEXT NOT NULL, "
                    + "status TEXT NOT NULL, totalAmount REAL NOT NULL)");
            statement.executeUpdate("INSERT INTO orders_Orders "
                    + "(customerName, orderDate, status, totalAmount) VALUES "
                    + "('Maya', '2026-08-01', 'Pending', 19.98), "
                    + "('Noah', '2026-08-02', 'Out For Delivery', 24.50), "
                    + "('Zara', '2026-08-03', 'Cancelled', 11.25)");
        }
    }

    @AfterAll
    static void removeDatabase() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void deliveryRowsMirrorOrderRowsAndStatusChangesWriteBack() throws Exception {
        DeliveryDAO.ensureSchemaAndSync();

        assertEquals(3, DeliveryDAO.getDelivery().size());
        assertTrue(DeliveryDAO.assignDriver(1, "Iman"));
        assertEquals("Out For Delivery", orderStatus(1));
        assertEquals("Out For Delivery", deliveryStatus(1));

        assertTrue(DeliveryDAO.markDelivered(1));
        assertEquals("Delivered", orderStatus(1));
        assertEquals("Delivered", deliveryStatus(1));

        execute("UPDATE orders_Orders SET status = 'Preparing' WHERE orderID = 2");
        assertEquals("Preparing", deliveryStatus(2));

        execute("INSERT INTO orders_Orders "
                + "(customerName, orderDate, status, totalAmount) "
                + "VALUES ('Liam', '2026-08-04', 'Confirmed', 16.75)");
        assertEquals("Confirmed", deliveryStatus(4));
    }

    private static String orderStatus(int orderID) throws Exception {
        return status("SELECT status FROM orders_Orders WHERE orderID = " + orderID);
    }

    private static String deliveryStatus(int orderID) throws Exception {
        return status("SELECT status FROM delivery_Deliveries WHERE orderID = " + orderID);
    }

    private static String status(String sql) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getString(1) : null;
        }
    }

    private static void execute(String sql) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
