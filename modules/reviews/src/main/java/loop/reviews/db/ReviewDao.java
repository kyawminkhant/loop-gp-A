package loop.reviews.db;

import loop.reviews.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the "reviews_reviews" table. */
public class ReviewDao {

    private Connection conn() { return Database.get().getConnection(); }

    private Review map(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setProductId(rs.getInt("product_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setRating(rs.getInt("rating"));
        r.setCommentText(rs.getString("comment_text"));
        r.setImageUrl(rs.getString("image_url"));
        r.setCreatedAt(rs.getLong("created_at"));
        r.setStatus(rs.getString("status"));
        r.setHelpfulCount(rs.getInt("helpful_count"));
        r.setUnhelpfulCount(rs.getInt("unhelpful_count"));
        r.setEditDurationSeconds(rs.getInt("edit_duration_seconds"));
        // customerName included when the query joins users (aliased as customer_name)
        try {
            String cn = rs.getString("customer_name");
            r.setCustomerName(cn);
        } catch (SQLException ignore) { /* column not present in this query */ }
        return r;
    }

    private static final String SELECT_JOIN =
        "SELECT r.*, u.name AS customer_name " +
        "FROM reviews_reviews r JOIN reviews_users u ON u.id = r.customer_id ";

    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOIN + "ORDER BY r.created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll reviews_reviews failed", e);
        }
        return list;
    }

    public Review findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_JOIN + "WHERE r.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById review failed", e);
        }
    }

    /**
     * FR6: reviews for a product with sort + optional filters.
     * @param sort one of "date", "rating_desc", "rating_asc", "helpful".
     * @param minStars 0 to ignore, else 1..5 exact star filter.
     * @param keyword null/blank to ignore, else case-insensitive substring on
     *                comment, customer name, or customer email.
     * Only Active reviews are shown to normal users.
     */
    public List<Review> findByProduct(int productId, String sort, int minStars, String keyword) {
        StringBuilder sql = new StringBuilder(SELECT_JOIN + "WHERE r.product_id=? AND r.status='Active'");
        List<Object> params = new ArrayList<>();
        params.add(productId);
        if (minStars >= 1 && minStars <= 5) {
            sql.append(" AND r.rating=?");
            params.add(minStars);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String match = "%" + keyword.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(r.comment_text) LIKE ?")
               .append(" OR LOWER(u.name) LIKE ?")
               .append(" OR LOWER(u.email) LIKE ?)");
            params.add(match);
            params.add(match);
            params.add(match);
        }
        switch (sort == null ? "helpful" : sort) {
            case "date":
                sql.append(" ORDER BY r.created_at DESC");
                break;
            case "rating":
            case "rating_desc":
                sql.append(" ORDER BY r.rating DESC, r.created_at DESC");
                break;
            case "rating_asc":
                sql.append(" ORDER BY r.rating ASC, r.created_at DESC");
                break;
            default:
                sql.append(" ORDER BY (r.helpful_count - r.unhelpful_count) DESC, r.created_at DESC");
        }
        List<Review> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByProduct failed", e);
        }
        return list;
    }

    /** Reviews written by a given customer (My Reviews - FR4/FR5). */
    public List<Review> findByCustomer(int customerId) {
        List<Review> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                SELECT_JOIN + "WHERE r.customer_id=? ORDER BY r.created_at DESC")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByCustomer failed", e);
        }
        return list;
    }

    /** FR10: duplicate detection (same customer + product). */
    public boolean existsForCustomerAndProduct(int customerId, int productId) {
        String sql = "SELECT 1 FROM reviews_reviews WHERE customer_id=? AND product_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("duplicate check failed", e);
        }
    }

    /** Per-star counts (index 0 unused; 1..5 hold counts) for the distribution chart. */
    public int[] ratingDistribution(int productId) {
        int[] counts = new int[6];
        String sql = "SELECT rating, COUNT(*) c FROM reviews_reviews WHERE product_id=? AND status='Active' GROUP BY rating";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int star = rs.getInt("rating");
                    if (star >= 1 && star <= 5) counts[star] = rs.getInt("c");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ratingDistribution failed", e);
        }
        return counts;
    }

    public int insert(Review r) {
        String sql = "INSERT INTO reviews_reviews(product_id,customer_id,rating,comment_text,image_url," +
                     "created_at,status,helpful_count,unhelpful_count,edit_duration_seconds) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getProductId());
            ps.setInt(2, r.getCustomerId());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getCommentText());
            ps.setString(5, r.getImageUrl());
            ps.setLong(6, r.getCreatedAt());
            ps.setString(7, r.getStatus() == null ? Review.ACTIVE : r.getStatus());
            ps.setInt(8, r.getHelpfulCount());
            ps.setInt(9, r.getUnhelpfulCount());
            ps.setInt(10, r.getEditDurationSeconds() == 0 ? 300 : r.getEditDurationSeconds());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
            return r.getId();
        } catch (SQLException e) {
            throw new RuntimeException("insert review failed", e);
        }
    }

    /** Update rating + comment (edit). Timestamp is refreshed to now (FR4). */
    public void updateContent(int reviewId, int rating, String comment) {
        String sql = "UPDATE reviews_reviews SET rating=?, comment_text=?, created_at=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setLong(3, System.currentTimeMillis());
            ps.setInt(4, reviewId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update review failed", e);
        }
    }

    /** Customer edit that can move newly-detected content into the hidden moderation queue. */
    public void updateCustomerContent(int reviewId, int rating, String comment, String status) {
        String sql = "UPDATE reviews_reviews SET rating=?, comment_text=?, created_at=?, status=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setLong(3, System.currentTimeMillis());
            ps.setString(4, status);
            ps.setInt(5, reviewId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("customer review update failed", e);
        }
    }

    public void updateStatus(int reviewId, String status) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE reviews_reviews SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, reviewId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update status failed", e);
        }
    }

    public void delete(int id) {
        // Remove dependent votes first to satisfy FK constraints.
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_helpful_votes WHERE review_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete votes failed", e);
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM reviews_review_flags WHERE review_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete review flags failed", e);
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM reviews_admin_moderation_log WHERE review_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete review moderation history failed", e);
        }
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_reviews WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete review failed", e);
        }
    }
}
