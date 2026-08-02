package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {

    private static final String URL = "jdbc:sqlite:" +
        System.getProperty("loop.db.path", "database/loop.db");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
