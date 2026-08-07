package loop.reviews.db;

import loop.reviews.model.HelpfulVote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Data-access object for the "reviews_helpful_votes" table (FR7). */
public class HelpfulVoteDao {

    public enum ToggleResult { ADDED, REMOVED, SWITCHED }

    private final Connection suppliedConnection;

    public HelpfulVoteDao() {
        this(null);
    }

    HelpfulVoteDao(Connection suppliedConnection) {
        this.suppliedConnection = suppliedConnection;
    }

    private Connection conn() {
        return suppliedConnection == null
                ? Database.get().getConnection()
                : suppliedConnection;
    }

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

    /** Loads a customer's current choices in one query for efficient card rendering. */
    public Map<Integer, String> findVoteTypesByCustomer(int customerId) {
        Map<Integer, String> votes = new HashMap<>();
        String sql = "SELECT review_id,vote_type FROM reviews_helpful_votes WHERE customer_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    votes.put(rs.getInt("review_id"), rs.getString("vote_type"));
                }
            }
            return votes;
        } catch (SQLException e) {
            throw new RuntimeException("load customer votes failed", e);
        }
    }

    /**
     * Adds a choice, removes it when selected again, or switches to the other
     * choice. The vote row and displayed counters change in one transaction.
     */
    public ToggleResult toggle(int reviewId, int customerId, String requestedType) {
        validateVoteType(requestedType);
        Connection connection = conn();
        boolean manageTransaction = true;
        try {
            manageTransaction = connection.getAutoCommit();
            if (manageTransaction) {
                connection.setAutoCommit(false);
            }

            String currentType = findVoteType(connection, reviewId, customerId);
            ToggleResult result = decideToggle(currentType, requestedType);
            if (result == ToggleResult.ADDED) {
                insertVote(connection, reviewId, customerId, requestedType);
                adjustCounter(connection, reviewId, requestedType, 1);
            } else if (result == ToggleResult.REMOVED) {
                deleteVote(connection, reviewId, customerId);
                adjustCounter(connection, reviewId, requestedType, -1);
            } else {
                updateVote(connection, reviewId, customerId, requestedType);
                adjustCounter(connection, reviewId, currentType, -1);
                adjustCounter(connection, reviewId, requestedType, 1);
            }

            if (manageTransaction) {
                connection.commit();
            }
            return result;
        } catch (SQLException e) {
            if (manageTransaction) {
                try { connection.rollback(); } catch (SQLException ignored) { }
            }
            throw new RuntimeException("toggle review vote failed", e);
        } finally {
            if (manageTransaction) {
                try { connection.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        }
    }

    static ToggleResult decideToggle(String currentType, String requestedType) {
        validateVoteType(requestedType);
        if (currentType == null) {
            return ToggleResult.ADDED;
        }
        return requestedType.equals(currentType) ? ToggleResult.REMOVED : ToggleResult.SWITCHED;
    }

    private static void validateVoteType(String voteType) {
        if (!HelpfulVote.HELPFUL.equals(voteType) && !HelpfulVote.UNHELPFUL.equals(voteType)) {
            throw new IllegalArgumentException("Unsupported vote type: " + voteType);
        }
    }

    private String findVoteType(Connection connection, int reviewId, int customerId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT vote_type FROM reviews_helpful_votes WHERE review_id=? AND customer_id=?")) {
            ps.setInt(1, reviewId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("vote_type") : null;
            }
        }
    }

    private void insertVote(
            Connection connection,
            int reviewId,
            int customerId,
            String voteType) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO reviews_helpful_votes(review_id,customer_id,vote_type,created_at) " +
                        "VALUES(?,?,?,?)")) {
            ps.setInt(1, reviewId);
            ps.setInt(2, customerId);
            ps.setString(3, voteType);
            ps.setLong(4, Instant.now().toEpochMilli());
            ps.executeUpdate();
        }
    }

    private void updateVote(
            Connection connection,
            int reviewId,
            int customerId,
            String voteType) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE reviews_helpful_votes SET vote_type=?,created_at=? " +
                        "WHERE review_id=? AND customer_id=?")) {
            ps.setString(1, voteType);
            ps.setLong(2, Instant.now().toEpochMilli());
            ps.setInt(3, reviewId);
            ps.setInt(4, customerId);
            ps.executeUpdate();
        }
    }

    private void deleteVote(Connection connection, int reviewId, int customerId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM reviews_helpful_votes WHERE review_id=? AND customer_id=?")) {
            ps.setInt(1, reviewId);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    private void adjustCounter(
            Connection connection,
            int reviewId,
            String voteType,
            int amount) throws SQLException {
        validateVoteType(voteType);
        String column = HelpfulVote.HELPFUL.equals(voteType)
                ? "helpful_count" : "unhelpful_count";
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE reviews_reviews SET " + column + "=MAX(0," + column + "+?) WHERE id=?")) {
            ps.setInt(1, amount);
            ps.setInt(2, reviewId);
            ps.executeUpdate();
        }
    }
}
