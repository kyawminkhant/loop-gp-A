
import Database.DBconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.DeliveryDetails;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DeliveryDetailsDAO {

    private DeliveryDetailsDAO() { }

    public static ObservableList<DeliveryDetails> getDelivery() {
        ObservableList<DeliveryDetails> deliveries = FXCollections.observableArrayList();

        try {
            DeliveryDAO.ensureSchemaAndSync();
            try (Connection connection = DBconnection.getConnection()) {
                boolean hasCustomers = tableExists(connection, "customer_Customers");
                String sql = "SELECT delivery.deliveryID, orders.orderID, orders.customerName, "
                        + (hasCustomers
                            ? "customer.deliveryAddress, "
                            : "NULL AS deliveryAddress, ")
                        + "orders.status, delivery.driver "
                        + "FROM delivery_Deliveries delivery "
                        + "JOIN orders_Orders orders ON orders.orderID = delivery.orderID "
                        + (hasCustomers
                            ? "LEFT JOIN customer_Customers customer ON customer.customerID = orders.customerID "
                            : "")
                        + "ORDER BY orders.orderDate DESC, orders.orderID DESC";
                try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                    while (result.next()) {
                        deliveries.add(new DeliveryDetails(
                                result.getString("deliveryID"),
                                result.getInt("orderID"),
                                result.getString("customerName"),
                                result.getString("deliveryAddress"),
                                result.getString("status"),
                                result.getString("driver")
                        ));
                    }
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return deliveries;
    }

    private static boolean tableExists(Connection connection, String tableName)
            throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
