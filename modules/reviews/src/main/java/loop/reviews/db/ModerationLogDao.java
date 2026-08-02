package loop.reviews.db;

import loop.reviews.model.ModerationLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the "reviews_admin_moderation_log" table (FR8 audit trail). */
public class ModerationLogDao {

    private Connection conn() { return Database.get().getConnection(); }

    private static final String SELECT_JOIN =
        "SELECT l.*, u.name AS admin_name FROM reviews_admin_moderation_log l " +
        "JOIN reviews_users u ON u.id = l.admin_id ";

    private ModerationLog map(ResultSet rs) throws SQLException {
        ModerationLog m = new ModerationLog();
        m.setId(rs.getInt("id"));
        m.setAdminId(rs.getInt("admin_id"));
        m.setReviewId(rs.getInt("review_id"));
        m.setAction(rs.getString("action"));
        m.setCreatedAt(rs.getLong("created_at"));
        m.setNotes(rs.getString("notes"));
        try { m.setAdminName(rs.getString("admin_name")); } catch (SQLException ignore) { }
        return m;
    }

    public List<ModerationLog> findAll() {
        List<ModerationLog> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOIN + "ORDER BY l.created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll logs failed", e);
        }
        return list;
    }

    public int insert(ModerationLog m) {
        String sql = "INSERT INTO reviews_admin_moderation_log(admin_id,review_id,action,created_at,notes) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getAdminId());
            ps.setInt(2, m.getReviewId());
            ps.setString(3, m.getAction());
            ps.setLong(4, Instant.now().toEpochMilli());
            ps.setString(5, m.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m.getId();
        } catch (SQLException e) {
            throw new RuntimeException("insert log failed", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_admin_moderation_log WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete log failed", e);
        }
    }
}
