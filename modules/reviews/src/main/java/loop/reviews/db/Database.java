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
    private static final String DB_FILE = System.getProperty("loop.db.path", "database/loop.db");

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
                "CREATE TABLE IF NOT EXISTS product_Chef (" +
                "  chefID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  chefName TEXT NOT NULL," +
                "  \"chefRating&ReviewID\" INTEGER NOT NULL DEFAULT 0," +
                "  chefDescription TEXT NOT NULL DEFAULT ''," +
                "  chefTag1 TEXT NOT NULL DEFAULT '', chefTag2 TEXT NOT NULL DEFAULT ''," +
                "  chefTag3 TEXT NOT NULL DEFAULT '', chefImage TEXT NOT NULL DEFAULT ''," +
                "  chefEmail TEXT NOT NULL DEFAULT '', chefTel TEXT NOT NULL DEFAULT ''" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Products (" +
                "  productID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  productName TEXT NOT NULL, shortDescription TEXT NOT NULL," +
                "  extendedDescription TEXT NOT NULL, cost REAL NOT NULL, price REAL NOT NULL," +
                "  status INTEGER NOT NULL DEFAULT 1, spiceLevel INTEGER NOT NULL DEFAULT 0," +
                "  country TEXT NOT NULL, createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  updatedDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, chefID INTEGER NOT NULL," +
                "  sourceModule TEXT NOT NULL DEFAULT 'product'," +
                "  stockQuantity INTEGER NOT NULL DEFAULT 100," +
                "  FOREIGN KEY (chefID) REFERENCES product_Chef(chefID)" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Category (" +
                "  categoryID INTEGER PRIMARY KEY AUTOINCREMENT, chosenSortBy TEXT NOT NULL DEFAULT ''," +
                "  chosenDietary TEXT NOT NULL, chosenHealthGoal TEXT NOT NULL," +
                "  chosenCuisines TEXT NOT NULL, productID INTEGER NOT NULL," +
                "  FOREIGN KEY (productID) REFERENCES product_Products(productID) ON DELETE CASCADE" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Ratings (" +
                "  rateID INTEGER PRIMARY KEY AUTOINCREMENT, rating REAL NOT NULL DEFAULT 0," +
                "  noPeople INTEGER NOT NULL DEFAULT 0, productID INTEGER NOT NULL," +
                "  FOREIGN KEY (productID) REFERENCES product_Products(productID) ON DELETE CASCADE" +
                ");");

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
                "CREATE TABLE IF NOT EXISTS reviews_orders (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  customer_id INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  product_id  INTEGER NOT NULL REFERENCES product_Products(productID)," +
                "  order_date  TEXT    NOT NULL" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_reviews (" +
                "  id                    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  product_id            INTEGER NOT NULL REFERENCES product_Products(productID)," +
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

            st.execute(
                "CREATE VIEW IF NOT EXISTS reviews_products AS " +
                "SELECT product.productID AS id, product.productName AS name, product.price, " +
                "product.stockQuantity AS stock, COALESCE(category.category, 'Meals') AS category, " +
                "COALESCE(rating.average_rating, 0) AS average_rating " +
                "FROM product_Products product " +
                "LEFT JOIN (SELECT productID, MAX(chosenCuisines) AS category " +
                "           FROM product_Category GROUP BY productID) category " +
                "  ON category.productID = product.productID " +
                "LEFT JOIN (SELECT productID, CASE WHEN SUM(noPeople)=0 THEN 0 " +
                "                  ELSE SUM(rating*noPeople)/SUM(noPeople) END AS average_rating " +
                "           FROM product_Ratings GROUP BY productID) rating " +
                "  ON rating.productID = product.productID " +
                "WHERE product.status=1 OR product.sourceModule='reviews';");
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

            int[] products = ensureSampleProducts();

            // Orders establish purchase eligibility (verifyPurchase / FR2).
            // Tasmia(2) bought products 1,2,3 ; Daniel(3) bought 1,4 ; Priya(4) bought 2,5.
            String d = "2026-07-10";
            st.executeUpdate("INSERT INTO reviews_orders(customer_id,product_id,order_date) VALUES " +
                "(2," + products[0] + ",'" + d + "'),(2," + products[1] + ",'" + d + "')," +
                "(2," + products[2] + ",'" + d + "'),(3," + products[0] + ",'" + d + "')," +
                "(3," + products[3] + ",'" + d + "'),(4," + products[1] + ",'" + d + "')," +
                "(4," + products[4] + ",'" + d + "');");

            // A few seeded reviews. Older created_at so most are already locked,
            // demonstrating the edit window; one is fresh so editing can be tried.
            long tenMinAgo = now - (10L * 60 * 1000);
            long dayAgo = now - (24L * 60 * 60 * 1000);
            st.executeUpdate("INSERT INTO reviews_reviews(product_id,customer_id,rating,comment_text,image_url,created_at,status,helpful_count,unhelpful_count,edit_duration_seconds) VALUES " +
                "(" + products[0] + ",3,5,'Absolutely delicious, cooked perfectly and arrived hot.',NULL," + dayAgo + ",'Active',4,0,300)," +
                "(" + products[0] + ",2,4,'Really tasty salmon, portion could be a little bigger.',NULL," + tenMinAgo + ",'Active',2,1,300)," +
                "(" + products[1] + ",4,5,'Best vegan bowl I have had, so fresh and filling.',NULL," + dayAgo + ",'Active',6,0,300)," +
                "(" + products[2] + ",2,3,'Broth was good but noodles were slightly overcooked.',NULL," + dayAgo + ",'Active',1,0,300)," +
                "(" + products[4] + ",4,4,'Lovely dessert, sweet and creamy.',NULL," + dayAgo + ",'Active',3,0,300);");
        }

        // Recalculate seeded product averages via the DAO logic.
        new ProductDao().recalculateAllAverages();
        System.out.println("[Database] Sample data seeded.");
    }

    private int[] ensureSampleProducts() throws SQLException {
        int chefId = 0;
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet result = statement.executeQuery("SELECT MIN(chefID) FROM product_Chef")) {
            if (result.next()) {
                chefId = result.getInt(1);
            }
        }
        if (chefId == 0) {
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO product_Chef " +
                    "(chefName,\"chefRating&ReviewID\",chefDescription,chefTag1,chefTag2,chefTag3," +
                    "chefImage,chefEmail,chefTel) VALUES " +
                    "('LOOP Reviews',0,'Reviews catalogue chef','','','','','reviews@loop.com','')",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.executeUpdate();
                try (java.sql.ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    chefId = keys.getInt(1);
                }
            }
        }

        String[] names = {
            "Grilled Salmon Bowl", "Vegan Buddha Bowl", "Spicy Chicken Ramen",
            "Classic Beef Burger", "Mango Sticky Rice"
        };
        double[] prices = {12.99, 9.50, 11.25, 10.00, 6.75};
        int[] stocks = {40, 60, 25, 0, 50};
        String[] categories = {"Mains", "Vegan", "Mains", "Mains", "Desserts"};
        int[] ids = new int[names.length];

        for (int index = 0; index < names.length; index++) {
            try (java.sql.PreparedStatement find = connection.prepareStatement(
                    "SELECT productID FROM product_Products WHERE productName=? LIMIT 1")) {
                find.setString(1, names[index]);
                try (java.sql.ResultSet result = find.executeQuery()) {
                    if (result.next()) {
                        ids[index] = result.getInt(1);
                    }
                }
            }

            if (ids[index] == 0) {
                try (java.sql.PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO product_Products " +
                        "(productName,shortDescription,extendedDescription,cost,price,status,spiceLevel," +
                        "country,chefID,sourceModule,stockQuantity) VALUES " +
                        "(?,'Reviews catalogue item','Reviews demonstration product',?,?,0,0," +
                        "'United Kingdom',?,'reviews',?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, names[index]);
                    insert.setDouble(2, Math.round(prices[index] * 50.0) / 100.0);
                    insert.setDouble(3, prices[index]);
                    insert.setInt(4, chefId);
                    insert.setInt(5, stocks[index]);
                    insert.executeUpdate();
                    try (java.sql.ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        ids[index] = keys.getInt(1);
                    }
                }

                try (java.sql.PreparedStatement category = connection.prepareStatement(
                        "INSERT INTO product_Category " +
                        "(chosenSortBy,chosenDietary,chosenHealthGoal,chosenCuisines,productID) " +
                        "VALUES('','Not specified','Balanced Meals',?,?)")) {
                    category.setString(1, categories[index]);
                    category.setInt(2, ids[index]);
                    category.executeUpdate();
                }
                try (java.sql.PreparedStatement rating = connection.prepareStatement(
                        "INSERT INTO product_Ratings(rating,noPeople,productID) VALUES(0,0,?)")) {
                    rating.setInt(1, ids[index]);
                    rating.executeUpdate();
                }
            }
        }
        return ids;
    }
}
