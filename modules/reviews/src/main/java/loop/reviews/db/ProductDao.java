package loop.reviews.db;

import loop.reviews.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the "reviews_products" table. */
public class ProductDao {

    private Connection conn() { return Database.get().getConnection(); }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt("stock"),
            rs.getString("category"),
            rs.getDouble("average_rating"));
    }

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reviews_products ORDER BY name")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll reviews_products failed", e);
        }
        return list;
    }

    public Product findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM reviews_products WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById product failed", e);
        }
    }

    public int insert(Product p) {
        String sql = "INSERT INTO reviews_products(name,price,stock,category,average_rating) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setInt(3, p.getStock());
            ps.setString(4, p.getCategory());
            ps.setDouble(5, p.getAverageRating());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p.getId();
        } catch (SQLException e) {
            throw new RuntimeException("insert product failed", e);
        }
    }

    public void update(Product p) {
        String sql = "UPDATE reviews_products SET name=?, price=?, stock=?, category=?, average_rating=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setInt(3, p.getStock());
            ps.setString(4, p.getCategory());
            ps.setDouble(5, p.getAverageRating());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update product failed", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_products WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete product failed", e);
        }
    }

    /**
     * FR9: recalculate the average rating for a product to nearest 0.1, counting
     * only Active reviews, and persist it. Returns the new average.
     */
    public double recalculateAverage(int productId) {
        String sql = "SELECT AVG(rating) AS avg FROM reviews_reviews WHERE product_id=? AND status='Active'";
        double avg = 0;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    avg = rs.getDouble("avg"); // 0 if null
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("recalculateAverage failed", e);
        }
        avg = Math.round(avg * 10.0) / 10.0; // nearest 0.1
        try (PreparedStatement ps = conn().prepareStatement("UPDATE reviews_products SET average_rating=? WHERE id=?")) {
            ps.setDouble(1, avg);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("persist average failed", e);
        }
        return avg;
    }

    public void recalculateAllAverages() {
        for (Product p : findAll()) {
            recalculateAverage(p.getId());
        }
    }
}
