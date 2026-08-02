package loop.reviews.db;

import loop.reviews.model.HelpfulVote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the "reviews_helpful_votes" table (FR7). */
public class HelpfulVoteDao {

    private Connection conn() { return Database.get().getConnection(); }

    public List<HelpfulVote> findAll() {
        List<HelpfulVote> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reviews_helpful_votes ORDER BY id")) {
            while (rs.next()) {
                HelpfulVote v = new HelpfulVote();
                v.setId(rs.getInt("id"));
                v.setReviewId(rs.getInt("review_id"));
                v.setCustomerId(rs.getInt("customer_id"));
                v.setVoteType(rs.getString("vote_type"));
                v.setCreatedAt(rs.getLong("created_at"));
                list.add(v);
            }
        } catch (SQLException e) {
            throw new RuntimeException("findAll votes failed", e);
        }
        return list;
    }

    /** checkDuplicate(): true if this customer already voted on this review. */
    public boolean hasVoted(int reviewId, int customerId) {
        String sql = "SELECT 1 FROM reviews_helpful_votes WHERE review_id=? AND customer_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("hasVoted failed", e);
        }
    }

    public int insert(HelpfulVote v) {
        String sql = "INSERT INTO reviews_helpful_votes(review_id,customer_id,vote_type,created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getReviewId());
            ps.setInt(2, v.getCustomerId());
            ps.setString(3, v.getVoteType());
            ps.setLong(4, Instant.now().toEpochMilli());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) v.setId(keys.getInt(1));
            }
            return v.getId();
        } catch (SQLException e) {
            throw new RuntimeException("insert vote failed", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_helpful_votes WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete vote failed", e);
        }
    }
}
