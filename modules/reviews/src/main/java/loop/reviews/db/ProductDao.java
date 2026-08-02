package loop.reviews.db;

import loop.reviews.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reviews access to the shared Product catalogue through the reviews_products view. */
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
        Connection connection = conn();
        try {
            connection.setAutoCommit(false);
            int chefId;
            try (Statement st = connection.createStatement();
                 ResultSet result = st.executeQuery("SELECT MIN(chefID) FROM product_Chef")) {
                result.next();
                chefId = result.getInt(1);
            }

            String sql = "INSERT INTO product_Products "
                    + "(productName,shortDescription,extendedDescription,cost,price,status,spiceLevel,country,"
                    + "chefID,sourceModule,stockQuantity) VALUES(?,?,?,?,?,0,0,'United Kingdom',?,'reviews',?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getName());
                ps.setString(2, "Reviews catalogue item");
                ps.setString(3, "Product maintained through the shared LOOP catalogue.");
                ps.setDouble(4, Math.max(0, p.getPrice() * 0.5));
                ps.setDouble(5, p.getPrice());
                ps.setInt(6, chefId);
                ps.setInt(7, p.getStock());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) p.setId(keys.getInt(1));
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO product_Category "
                            + "(chosenSortBy,chosenDietary,chosenHealthGoal,chosenCuisines,productID) "
                            + "VALUES('','Not specified','Balanced Meals',?,?)")) {
                ps.setString(1, p.getCategory());
                ps.setInt(2, p.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO product_Ratings(rating,noPeople,productID) VALUES(?,?,?)")) {
                ps.setDouble(1, p.getAverageRating());
                ps.setInt(2, 0);
                ps.setInt(3, p.getId());
                ps.executeUpdate();
            }
            connection.commit();
            return p.getId();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) { }
            throw new RuntimeException("insert product failed", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) { }
        }
    }

    public void update(Product p) {
        String sql = "UPDATE product_Products SET productName=?, price=?, stockQuantity=?, "
                + "updatedDate=CURRENT_TIMESTAMP WHERE productID=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setInt(3, p.getStock());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
            try (PreparedStatement category = conn().prepareStatement(
                    "UPDATE product_Category SET chosenCuisines=? WHERE productID=?")) {
                category.setString(1, p.getCategory());
                category.setInt(2, p.getId());
                category.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("update product failed", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM product_Products WHERE productID=?")) {
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
        int reviewCount = 0;
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
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM reviews_reviews WHERE product_id=? AND status='Active'")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                reviewCount = rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("count product reviews failed", e);
        }

        int updated;
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE product_Ratings SET rating=?, noPeople=? WHERE productID=?")) {
            ps.setDouble(1, avg);
            ps.setInt(2, reviewCount);
            ps.setInt(3, productId);
            updated = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("persist average failed", e);
        }
        if (updated == 0) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO product_Ratings(rating,noPeople,productID) VALUES(?,?,?)")) {
                ps.setDouble(1, avg);
                ps.setInt(2, reviewCount);
                ps.setInt(3, productId);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("insert product average failed", e);
            }
        }
        return avg;
    }

    public void recalculateAllAverages() {
        for (Product p : findAll()) {
            recalculateAverage(p.getId());
        }
    }
}
