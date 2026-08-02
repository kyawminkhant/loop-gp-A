package loop.reviews.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * Owns the SQLite connection and schema for the Reviews & Ratings component.
 *
 *  - Creates every table with CREATE TABLE IF NOT EXISTS on first run.
 *  - Prints the ABSOLUTE path to loop.db on startup so it can be opened in
 *    "DB Browser for SQLite".
 *  - Seeds a small amount of realistic sample data the first time it runs, so
 *    screens are populated on first launch.
 *
 * A single shared Connection is used (the app is single-threaded on the JavaFX
 * thread). SQLite foreign-key enforcement is turned on.
 */
public final class Database {

    /** The database file is created in the current working directory. */
    private static final String DB_FILE = "database/loop.db";

    private static Database instance;
    private Connection connection;

    private Database() { }

    public static synchronized Database get() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /** Open the connection, create tables, print path, seed data. */
    public void init() {
        File dbFile = new File(DB_FILE).getAbsoluteFile();
        System.out.println("========================================================");
        System.out.println(" LOOP reviews_reviews - SQLite database file:");
        System.out.println("   " + dbFile.getAbsolutePath());
        System.out.println(" Open this file in 'DB Browser for SQLite' to inspect it.");
        System.out.println("========================================================");

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
            createTables();
            seedIfEmpty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database", e);
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database.init() has not been called.");
        }
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_users (" +
                "  id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name     TEXT    NOT NULL," +
                "  email    TEXT    NOT NULL UNIQUE," +          // unique emails
                "  password TEXT    NOT NULL," +
                "  role     TEXT    NOT NULL CHECK(role IN ('CUSTOMER','ADMIN'))," +
                "  address  TEXT" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_products (" +
                "  id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name           TEXT    NOT NULL," +
                "  price          REAL    NOT NULL CHECK(price > 0)," +      // positive price
                "  stock          INTEGER NOT NULL CHECK(stock >= 0)," +     // non-negative stock
                "  category       TEXT," +
                "  average_rating REAL    NOT NULL DEFAULT 0" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_orders (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  customer_id INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  product_id  INTEGER NOT NULL REFERENCES reviews_products(id)," +
                "  order_date  TEXT    NOT NULL" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_reviews (" +
                "  id                    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  product_id            INTEGER NOT NULL REFERENCES reviews_products(id)," +
                "  customer_id           INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  rating                INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5)," +
                "  comment_text          TEXT    NOT NULL," +
                "  image_url             TEXT," +
                "  created_at            INTEGER NOT NULL," +          // epoch millis
                "  status                TEXT    NOT NULL DEFAULT 'Active'," +
                "  helpful_count         INTEGER NOT NULL DEFAULT 0," +
                "  unhelpful_count       INTEGER NOT NULL DEFAULT 0," +
                "  edit_duration_seconds INTEGER NOT NULL DEFAULT 300," +
                "  UNIQUE(product_id, customer_id)" +                 // no duplicate reviews
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_helpful_votes (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  review_id   INTEGER NOT NULL REFERENCES reviews_reviews(id)," +
                "  customer_id INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  vote_type   TEXT    NOT NULL CHECK(vote_type IN ('helpful','unhelpful'))," +
                "  created_at  INTEGER NOT NULL," +
                "  UNIQUE(review_id, customer_id)" +                  // one vote per customer per review
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_admin_moderation_log (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  admin_id   INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  review_id  INTEGER NOT NULL REFERENCES reviews_reviews(id)," +
                "  action     TEXT    NOT NULL," +
                "  created_at INTEGER NOT NULL," +
                "  notes      TEXT" +
                ");");
        }
    }

    /** Insert sample rows only if the users table is empty. */
    private void seedIfEmpty() throws SQLException {
        boolean empty;
        try (Statement st = connection.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM reviews_users")) {
            rs.next();
            empty = rs.getInt(1) == 0;
        }
        if (!empty) {
            return;
        }
        System.out.println("[Database] Seeding sample data...");

        long now = Instant.now().toEpochMilli();

        try (Statement st = connection.createStatement()) {
            // Users (1 admin, 3 customers). Passwords are min 8 chars (FR1).
            st.executeUpdate("INSERT INTO reviews_users(name,email,password,role,address) VALUES " +
                "('Site Admin','admin@loop.com','admin123','ADMIN',NULL)," +
                "('Tasmia Biswas','tasmia@loop.com','password1','CUSTOMER','12 Baker St, London')," +
                "('Daniel Okafor','daniel@loop.com','password2','CUSTOMER','8 Oak Rd, Leeds')," +
                "('Priya Sharma','priya@loop.com','password3','CUSTOMER','5 Elm Ave, Bristol');");

            // Products (chef-prepared dishes). Prices > 0, stock >= 0.
            st.executeUpdate("INSERT INTO reviews_products(name,price,stock,category,average_rating) VALUES " +
                "('Grilled Salmon Bowl',12.99,40,'Mains',0)," +
                "('Vegan Buddha Bowl',9.50,60,'Vegan',0)," +
                "('Spicy Chicken Ramen',11.25,25,'Mains',0)," +
                "('Classic Beef Burger',10.00,0,'Mains',0)," +
                "('Mango Sticky Rice',6.75,50,'Desserts',0);");

            // Orders establish purchase eligibility (verifyPurchase / FR2).
            // Tasmia(2) bought products 1,2,3 ; Daniel(3) bought 1,4 ; Priya(4) bought 2,5.
            String d = "2026-07-10";
            st.executeUpdate("INSERT INTO reviews_orders(customer_id,product_id,order_date) VALUES " +
                "(2,1,'" + d + "'),(2,2,'" + d + "'),(2,3,'" + d + "')," +
                "(3,1,'" + d + "'),(3,4,'" + d + "')," +
                "(4,2,'" + d + "'),(4,5,'" + d + "');");

            // A few seeded reviews. Older created_at so most are already locked,
            // demonstrating the edit window; one is fresh so editing can be tried.
            long tenMinAgo = now - (10L * 60 * 1000);
            long dayAgo = now - (24L * 60 * 60 * 1000);
            st.executeUpdate("INSERT INTO reviews_reviews(product_id,customer_id,rating,comment_text,image_url,created_at,status,helpful_count,unhelpful_count,edit_duration_seconds) VALUES " +
                "(1,3,5,'Absolutely delicious, cooked perfectly and arrived hot.',NULL," + dayAgo + ",'Active',4,0,300)," +
                "(1,2,4,'Really tasty salmon, portion could be a little bigger.',NULL," + tenMinAgo + ",'Active',2,1,300)," +
                "(2,4,5,'Best vegan bowl I have had, so fresh and filling.',NULL," + dayAgo + ",'Active',6,0,300)," +
                "(3,2,3,'Broth was good but noodles were slightly overcooked.',NULL," + dayAgo + ",'Active',1,0,300)," +
                "(5,4,4,'Lovely dessert, sweet and creamy.',NULL," + dayAgo + ",'Active',3,0,300);");
        }

        // Recalculate seeded product averages via the DAO logic.
        new ProductDao().recalculateAllAverages();
        System.out.println("[Database] Sample data seeded.");
    }
}
