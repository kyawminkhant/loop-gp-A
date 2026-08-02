package loop.reviews.db;

import loop.reviews.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the "reviews_orders" table (purchase eligibility, FR2/FR3). */
public class OrderDao {

    private Connection conn() { return Database.get().getConnection(); }

    private Order map(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            rs.getInt("product_id"),
            rs.getString("order_date"));
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reviews_orders ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll reviews_orders failed", e);
        }
        return list;
    }

    /** Products this customer has purchased (used to gate review submission). */
    public List<Integer> findPurchasedProductIds(int customerId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT product_id FROM reviews_orders WHERE customer_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findPurchasedProductIds failed", e);
        }
        return ids;
    }

    /** verifyPurchase(): true if this customer bought this product. */
    public boolean verifyPurchase(int customerId, int productId) {
        String sql = "SELECT 1 FROM reviews_orders WHERE customer_id=? AND product_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("verifyPurchase failed", e);
        }
    }

    public int insert(Order o) {
        String sql = "INSERT INTO reviews_orders(customer_id,product_id,order_date) VALUES(?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getCustomerId());
            ps.setInt(2, o.getProductId());
            ps.setString(3, o.getOrderDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) o.setId(keys.getInt(1));
            }
            return o.getId();
        } catch (SQLException e) {
            throw new RuntimeException("insert order failed", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_orders WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete order failed", e);
        }
    }
}
