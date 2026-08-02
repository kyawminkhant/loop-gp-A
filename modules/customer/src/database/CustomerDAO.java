package database;

import models.Customer;
import models.CustomerPreference;
import models.OrderHistoryItem;
import utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerDAO {

    public boolean emailExists(String email) {
        String sql = "SELECT personID FROM customer_People WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean idCardExists(String idCardNo) {
        String sql = "SELECT customerID FROM customer_Customers WHERE idCardNo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCardNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExistsForOtherPerson(String email, String currentPersonID) {
        String sql = "SELECT personID FROM customer_People WHERE email = ? AND personID <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, currentPersonID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean idCardExistsForOtherCustomer(String idCardNo, String currentCustomerID) {
        String sql = "SELECT customerID FROM customer_Customers WHERE idCardNo = ? AND customerID <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCardNo);
            ps.setString(2, currentCustomerID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean registerCustomer(String name, String email, String mobile, String password,
                                    String deliveryAddress, String idCardNo, String idCardImagePath) {

        String personID = UUID.randomUUID().toString();
        String customerID = UUID.randomUUID().toString();
        String preferenceID = UUID.randomUUID().toString();
        String hashedPassword = PasswordUtil.hash(password);

        String insertPerson = "INSERT INTO customer_People (personID, name, email, mobile, passwordHash) VALUES (?, ?, ?, ?, ?)";
        String insertCustomer = "INSERT INTO customer_Customers (customerID, personID, deliveryAddress, idCardNo, idCardImagePath, status) VALUES (?, ?, ?, ?, ?, 'Active')";
        String insertPreference = "INSERT INTO customer_CustomerPreference (preferenceID, customerID, favoriteCategories, notificationSettings, deliveryInstructions) VALUES (?, ?, 'General', 'Enabled', '')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(insertPerson)) {
                ps1.setString(1, personID);
                ps1.setString(2, name);
                ps1.setString(3, email);
                ps1.setString(4, mobile);
                ps1.setString(5, hashedPassword);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(insertCustomer)) {
                ps2.setString(1, customerID);
                ps2.setString(2, personID);
                ps2.setString(3, deliveryAddress);
                ps2.setString(4, idCardNo);
                ps2.setString(5, idCardImagePath);
                ps2.executeUpdate();
            }

            try (PreparedStatement ps3 = conn.prepareStatement(insertPreference)) {
                ps3.setString(1, preferenceID);
                ps3.setString(2, customerID);
                ps3.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Customer authenticate(String email, String password) {
        String sql = """
            SELECT c.customerID, c.deliveryAddress, c.idCardNo, c.idCardImagePath, c.status,
                   p.personID, p.name, p.email, p.mobile, p.passwordHash
            FROM customer_Customers c
            JOIN customer_People p ON c.personID = p.personID
            WHERE p.email = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("passwordHash");
                    if (PasswordUtil.matches(password, storedHash)) {
                        if ("Inactive".equalsIgnoreCase(rs.getString("status"))) {
                            return null;
                        }
                        return mapRowToCustomer(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Customer getCustomerById(String customerID) {
        String sql = """
            SELECT c.customerID, c.deliveryAddress, c.idCardNo, c.idCardImagePath, c.status,
                   p.personID, p.name, p.email, p.mobile, p.passwordHash
            FROM customer_Customers c
            JOIN customer_People p ON c.personID = p.personID
            WHERE c.customerID = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProfile(String customerID, String personID, String name,
                                 String mobile, String deliveryAddress) {
        String updatePerson = "UPDATE customer_People SET name = ?, mobile = ? WHERE personID = ?";
        String updateCustomer = "UPDATE customer_Customers SET deliveryAddress = ? WHERE customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(updatePerson)) {
                ps1.setString(1, name);
                ps1.setString(2, mobile);
                ps1.setString(3, personID);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateCustomer)) {
                ps2.setString(1, deliveryAddress);
                ps2.setString(2, customerID);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean superAdminUpdateIdentity(String customerID, String personID, String newEmail, String newIdCardNo) {
        String updateEmail = "UPDATE customer_People SET email = ? WHERE personID = ?";
        String updateIdCard = "UPDATE customer_Customers SET idCardNo = ? WHERE customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(updateEmail)) {
                ps1.setString(1, newEmail);
                ps1.setString(2, personID);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateIdCard)) {
                ps2.setString(1, newIdCardNo);
                ps2.setString(2, customerID);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Full Super Admin update: identity, contact, status and preferences.
     */
    public boolean superAdminUpdateCustomer(Customer customer,
                                            String favoriteCategories,
                                            String notificationSettings,
                                            String deliveryInstructions) {
        String updatePerson = "UPDATE customer_People SET name = ?, email = ?, mobile = ? WHERE personID = ?";
        String updateCustomer = """
            UPDATE customer_Customers
            SET deliveryAddress = ?, idCardNo = ?, status = ?
            WHERE customerID = ?
        """;
        String updatePref = """
            UPDATE customer_CustomerPreference
            SET favoriteCategories = ?, notificationSettings = ?, deliveryInstructions = ?
            WHERE customerID = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(updatePerson)) {
                ps1.setString(1, customer.getName());
                ps1.setString(2, customer.getEmail());
                ps1.setString(3, customer.getMobile());
                ps1.setString(4, customer.getPersonID());
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateCustomer)) {
                ps2.setString(1, customer.getDeliveryAddress());
                ps2.setString(2, customer.getIdCardNo());
                ps2.setString(3, customer.getStatus());
                ps2.setString(4, customer.getCustomerID());
                ps2.executeUpdate();
            }

            try (PreparedStatement ps3 = conn.prepareStatement(updatePref)) {
                ps3.setString(1, favoriteCategories);
                ps3.setString(2, notificationSettings);
                ps3.setString(3, deliveryInstructions);
                ps3.setString(4, customer.getCustomerID());
                ps3.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CustomerPreference getPreference(String customerID) {
        String sql = "SELECT * FROM customer_CustomerPreference WHERE customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CustomerPreference pref = new CustomerPreference();
                    pref.setPreferenceID(rs.getString("preferenceID"));
                    pref.setCustomerID(rs.getString("customerID"));
                    pref.setFavoriteCategories(rs.getString("favoriteCategories"));
                    pref.setNotificationSettings(rs.getString("notificationSettings"));
                    pref.setDeliveryInstructions(rs.getString("deliveryInstructions"));
                    return pref;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean savePreference(CustomerPreference pref) {
        String sql = """
            UPDATE customer_CustomerPreference
            SET favoriteCategories = ?, notificationSettings = ?, deliveryInstructions = ?
            WHERE customerID = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pref.getFavoriteCategories());
            ps.setString(2, pref.getNotificationSettings());
            ps.setString(3, pref.getDeliveryInstructions());
            ps.setString(4, pref.getCustomerID());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deactivateCustomer(String customerID) {
        return setStatus(customerID, "Inactive");
    }

    public boolean toggleStatus(String customerID, String currentStatus) {
        String newStatus = "Active".equalsIgnoreCase(currentStatus) ? "Inactive" : "Active";
        return setStatus(customerID, newStatus);
    }

    private boolean setStatus(String customerID, String status) {
        String sql = "UPDATE customer_Customers SET status = ? WHERE customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, customerID);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderHistoryItem> getOrderHistory(String customerID) {
        List<OrderHistoryItem> orders = new ArrayList<>();
        String sql = "SELECT orderID, customerID, orderDate, status, totalAmount "
                + "FROM orders_Orders WHERE customerID = ? ORDER BY orderDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderHistoryItem item = new OrderHistoryItem();
                    item.setOrderID(rs.getString("orderID"));
                    item.setCustomerID(rs.getString("customerID"));
                    item.setOrderDate(rs.getString("orderDate"));
                    item.setStatus(rs.getString("status"));
                    item.setTotalAmount(rs.getDouble("totalAmount"));
                    orders.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Customer> getAllCustomers(String searchTerm) {
        List<Customer> customers = new ArrayList<>();

        String sql = """
            SELECT c.customerID, c.deliveryAddress, c.idCardNo, c.idCardImagePath, c.status,
                   p.personID, p.name, p.email, p.mobile,
                   pref.favoriteCategories, pref.notificationSettings, pref.deliveryInstructions
            FROM customer_Customers c
            JOIN customer_People p ON c.personID = p.personID
            LEFT JOIN customer_CustomerPreference pref ON pref.customerID = c.customerID
            WHERE p.name LIKE ? OR p.email LIKE ?
            ORDER BY p.name ASC
        """;

        String likeTerm = "%" + (searchTerm == null ? "" : searchTerm.trim()) + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, likeTerm);
            ps.setString(2, likeTerm);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer c = new Customer();
                    c.setCustomerID(rs.getString("customerID"));
                    c.setPersonID(rs.getString("personID"));
                    c.setDeliveryAddress(rs.getString("deliveryAddress"));
                    c.setIdCardNo(rs.getString("idCardNo"));
                    c.setIdCardImagePath(rs.getString("idCardImagePath"));
                    c.setStatus(rs.getString("status"));
                    c.setName(rs.getString("name"));
                    c.setEmail(rs.getString("email"));
                    c.setMobile(rs.getString("mobile"));
                    c.setFavoriteCategories(rs.getString("favoriteCategories"));
                    c.setNotificationSettings(rs.getString("notificationSettings"));
                    c.setDeliveryInstructions(rs.getString("deliveryInstructions"));
                    customers.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerID(rs.getString("customerID"));
        customer.setPersonID(rs.getString("personID"));
        customer.setDeliveryAddress(rs.getString("deliveryAddress"));
        customer.setIdCardNo(rs.getString("idCardNo"));
        customer.setIdCardImagePath(rs.getString("idCardImagePath"));
        customer.setStatus(rs.getString("status"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        customer.setMobile(rs.getString("mobile"));
        return customer;
    }
}
