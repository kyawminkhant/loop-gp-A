package loop.reviews.db;

import loop.reviews.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data-access object for the "reviews_users" table. */
public class UserDao {

    private Connection conn() { return Database.get().getConnection(); }

    private User map(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("address"));
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews_users ORDER BY id";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll reviews_users failed", e);
        }
        return list;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM reviews_users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById user failed", e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM reviews_users WHERE LOWER(email)=LOWER(?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByEmail user failed", e);
        }
    }

    public User findOrCreateCustomer(String name, String email) {
        User existing = findByEmail(email);
        if (existing != null) {
            return existing;
        }
        User customer = new User(0, name, email, "customer-session", "CUSTOMER", "");
        insert(customer);
        return customer;
    }

    /** Used by the login screen (FR1). Returns null if no match. */
    public User findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM reviews_users WHERE email = ? AND password = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("login query failed", e);
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM reviews_users WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("emailExists failed", e);
        }
    }

    /** Insert a new user; throws on duplicate email (UNIQUE constraint). */
    public int insert(User u) {
        String sql = "INSERT INTO reviews_users(name,email,password,role,address) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { u.setId(keys.getInt(1)); }
            }
            return u.getId();
        } catch (SQLException e) {
            throw new RuntimeException("insert user failed", e);
        }
    }

    public void update(User u) {
        String sql = "UPDATE reviews_users SET name=?, email=?, password=?, role=?, address=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getAddress());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("update user failed", e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM reviews_users WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("delete user failed", e);
        }
    }
}
