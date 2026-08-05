package loop.reviews.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Persistent customer reports for reviews that may need moderation. */
public class ReviewFlagDao {

    public boolean hasFlagged(int reviewId, int customerId) {
        String sql = "SELECT 1 FROM reviews_review_flags WHERE review_id=? AND customer_id=?";
        try (PreparedStatement statement =
                     Database.get().getConnection().prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            statement.setInt(2, customerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not check review flag", exception);
        }
    }

    public boolean flag(int reviewId, int customerId, String reason) {
        String sql = "INSERT OR IGNORE INTO reviews_review_flags " +
                "(review_id,customer_id,reason,created_at) VALUES(?,?,?,?)";
        try (PreparedStatement statement =
                     Database.get().getConnection().prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            statement.setInt(2, customerId);
            statement.setString(3, reason);
            statement.setLong(4, System.currentTimeMillis());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Could not flag review", exception);
        }
    }

    public List<String> findReasons(int reviewId) {
        List<String> reasons = new ArrayList<>();
        String sql = "SELECT reason || ' (' || COUNT(*) || ')' AS summary " +
                "FROM reviews_review_flags WHERE review_id=? AND resolved_at IS NULL GROUP BY reason " +
                "ORDER BY COUNT(*) DESC,reason";
        try (PreparedStatement statement =
                     Database.get().getConnection().prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reasons.add(result.getString("summary"));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not load review flag reasons", exception);
        }
        return reasons;
    }

    public int clearForReview(int reviewId, int adminId) {
        try (PreparedStatement statement = Database.get().getConnection().prepareStatement(
                "UPDATE reviews_review_flags SET resolved_at=?,resolved_by=? " +
                "WHERE review_id=? AND resolved_at IS NULL")) {
            statement.setLong(1, System.currentTimeMillis());
            statement.setInt(2, adminId);
            statement.setInt(3, reviewId);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Could not resolve review flags", exception);
        }
    }
}
