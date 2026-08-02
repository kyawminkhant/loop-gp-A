package LoopsFirstYearProject.LoopsFirstYearProject.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URLProduct = "jdbc:sqlite:database/loop.db";
    private static final String URLlocation = "jdbc:sqlite:database/loop.db";
 
    
    public static Connection getConnectionURLProduct() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC Driver not found! Did you add it to pom.xml or build.gradle?");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URLProduct);
        }
        
        
        public static Connection getConnectionURLlocation() throws SQLException {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ SQLite JDBC Driver not found! Did you add it to pom.xml or build.gradle?");
                e.printStackTrace();
            }
            return DriverManager.getConnection(URLlocation);
        }


    
    public static void testConnection1() {
        try (Connection conn = getConnectionURLlocation()) {
            if (conn != null) {
                System.out.println("✅ DB Connected successfully: " + conn);
            }
        } catch (Exception e) {
            System.err.println("❌ DB Connection failed!");
            e.printStackTrace();
        }
    }
    
    public static void testConnection2() {
        try (Connection conn = getConnectionURLProduct()) {
            if (conn != null) {
                System.out.println("✅ DB Connected successfully: " + conn);
            }
        } catch (Exception e) {
            System.err.println("❌ DB Connection failed!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testConnection1();
        testConnection2();
    }
}