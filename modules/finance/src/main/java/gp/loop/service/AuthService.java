package gp.loop.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import gp.loop.db.Database;

/**
 * Credential handling for the finance login (design: {@code FinanceAuthService}).
 * Passwords are stored as SHA-256 hashes in the {@code Users} table — never in plain text.
 */
public class AuthService {

    /** Hex-encoded SHA-256 of the given text. */
    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Creates an account. Returns {@code false} if the email is already registered. */
    public boolean register(String email, String password) throws Exception {
        String normalised = email.trim().toLowerCase();
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement check = c.prepareStatement(
                    "SELECT 1 FROM finance_Users WHERE Email = ?")) {
                check.setString(1, normalised);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }
            try (PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO finance_Users(Email, PasswordHash) VALUES (?,?)")) {
                insert.setString(1, normalised);
                insert.setString(2, sha256(password));
                insert.executeUpdate();
            }
        }
        return true;
    }

    /** Checks the email/password pair against the stored hash. */
    public boolean authenticate(String email, String password) throws Exception {
        String normalised = email.trim().toLowerCase();
        try (Connection c = Database.getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT PasswordHash FROM finance_Users WHERE Email = ?")) {
            ps.setString(1, normalised);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getString("PasswordHash").equals(sha256(password));
            }
        }
    }
}
