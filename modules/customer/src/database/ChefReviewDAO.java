package database;

import models.Chef;
import models.ChefReview;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChefReviewDAO {

    public List<Chef> getChefs() {
        List<Chef> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM customer_Chefs ORDER BY chefName ASC")) {
            while (rs.next()) {
                Chef c = new Chef();
                c.setChefID(rs.getString("chefID"));
                c.setChefName(rs.getString("chefName"));
                c.setSpeciality(rs.getString("speciality"));
                c.setAverageRating(rs.getDouble("averageRating"));
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean hasReviewed(String customerID, String chefID) {
        String sql = "SELECT reviewID FROM customer_ChefReviews WHERE customerID = ? AND chefID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            ps.setString(2, chefID);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean addReview(String customerID, String chefID, int rating, String reviewText) {
        String sql = "INSERT INTO customer_ChefReviews (reviewID, customerID, chefID, rating, reviewText, createdAt) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, customerID);
            ps.setString(3, chefID);
            ps.setInt(4, rating);
            ps.setString(5, reviewText);
            ps.setString(6, LocalDate.now().toString());
            ps.executeUpdate();
            updateAverageRating(conn, chefID);
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void updateAverageRating(Connection conn, String chefID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT AVG(rating) FROM customer_ChefReviews WHERE chefID = ?")) {
            ps.setString(1, chefID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = Math.round(rs.getDouble(1) * 10.0) / 10.0;
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE customer_Chefs SET averageRating = ? WHERE chefID = ?")) {
                        upd.setDouble(1, avg);
                        upd.setString(2, chefID);
                        upd.executeUpdate();
                    }
                }
            }
        }
    }

    public List<ChefReview> getReviewsByCustomer(String customerID) {
        List<ChefReview> list = new ArrayList<>();
        String sql = """
            SELECT cr.*, c.chefName FROM customer_ChefReviews cr
            JOIN customer_Chefs c ON cr.chefID = c.chefID
            WHERE cr.customerID = ? ORDER BY cr.createdAt DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChefReview r = new ChefReview();
                    r.setReviewID(rs.getString("reviewID"));
                    r.setCustomerID(rs.getString("customerID"));
                    r.setChefID(rs.getString("chefID"));
                    r.setRating(rs.getInt("rating"));
                    r.setReviewText(rs.getString("reviewText"));
                    r.setCreatedAt(rs.getString("createdAt"));
                    r.setChefName(rs.getString("chefName"));
                    list.add(r);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}