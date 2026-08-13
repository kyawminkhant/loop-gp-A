package services;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import model.WarehouseDelivery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Owns inbound warehouse deliveries and the address-to-warehouse assignment
 * shared by Inventory, Customers, and Products.
 */
public final class InventoryDeliveryService {

    private static final int CURRENT_STOCK_YEAR = Year.now().getValue();
    private static final DateTimeFormatter DATABASE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[][] WAREHOUSES = {
        {"WH-01", "Central Kitchen Warehouse", "Central London", "london,paddington,kensington,westminster,camden"},
        {"WH-02", "Kingston Warehouse", "Kingston", "kingston,surbiton,new malden"},
        {"WH-03", "Richmond Warehouse", "Richmond", "richmond,kew,twickenham"},
        {"WH-04", "Hounslow Warehouse", "Hounslow", "hounslow,feltham,hayes"},
        {"WH-05", "Croydon Warehouse", "Croydon", "croydon,purley,thornton heath"},
        {"WH-06", "Wimbledon Warehouse", "Wimbledon", "wimbledon,merton,mitcham"},
        {"WH-07", "Barnet Warehouse", "Barnet", "barnet,enfield,finchley"},
        {"WH-08", "Harrow Warehouse", "Harrow", "harrow,greenford,ruislip"},
        {"WH-09", "Romford Warehouse", "Romford", "romford,ilford,barking"},
        {"WH-10", "Wembley Warehouse", "Wembley", "wembley,brent,hillingdon"}
    };

    private static ScheduledExecutorService automaticUpdater;

    private InventoryDeliveryService() { }

    /** Creates the location schema and fills only missing current-year stock rows. */
    public static synchronized void ensureSchemaAndSeedData() throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                createTables(connection);
                seedWarehouses(connection);
                seedLocationStock(connection);
                seedInitialDeliveries(connection);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Starts one daemon updater for the combined application. */
    public static synchronized void startAutomaticUpdates() throws SQLException {
        ensureSchemaAndSeedData();
        if (automaticUpdater != null && !automaticUpdater.isShutdown()) {
            return;
        }

        automaticUpdater = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "inventory-delivery-updater");
            thread.setDaemon(true);
            return thread;
        });
        automaticUpdater.scheduleWithFixedDelay(() -> {
            try {
                runRandomUpdate();
            } catch (Exception exception) {
                System.err.println("Warehouse delivery update failed: "
                        + exception.getMessage());
            }
        }, 8, 10, TimeUnit.SECONDS);
    }

    public static List<WarehouseDelivery> loadDeliveries(String warehouseFilter)
            throws SQLException {
        ensureSchemaAndSeedData();
        String filter = warehouseFilter == null ? "" : warehouseFilter.trim();
        String sql = """
                SELECT delivery.deliveryID, delivery.warehouseID,
                       warehouse.name AS warehouseName,
                       warehouse.serviceArea,
                       delivery.ingredientID, ingredient.ingredientName,
                       delivery.quantity, delivery.status,
                       delivery.expectedAt, delivery.updatedAt
                FROM inventory_WarehouseDeliveries delivery
                JOIN inventory_Warehouses warehouse
                  ON warehouse.warehouseID = delivery.warehouseID
                JOIN product_Ingredient ingredient
                  ON ingredient.ingredientID = delivery.ingredientID
                WHERE (? = '' OR delivery.warehouseID = ?)
                ORDER BY CASE delivery.status
                           WHEN 'In Transit' THEN 0
                           WHEN 'Scheduled' THEN 1
                           ELSE 2
                         END,
                         delivery.updatedAt DESC, delivery.deliveryID DESC
                """;
        List<WarehouseDelivery> deliveries = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, filter);
            statement.setString(2, filter);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    deliveries.add(new WarehouseDelivery(
                            result.getInt("deliveryID"),
                            result.getString("warehouseID"),
                            result.getString("warehouseName"),
                            result.getString("serviceArea"),
                            result.getInt("ingredientID"),
                            result.getString("ingredientName"),
                            result.getInt("quantity"),
                            result.getString("status"),
                            result.getString("expectedAt"),
                            result.getString("updatedAt")));
                }
            }
        }
        return deliveries;
    }

    public static List<String> loadWarehouseChoices() throws SQLException {
        ensureSchemaAndSeedData();
        List<String> warehouses = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT warehouseID FROM inventory_Warehouses ORDER BY warehouseID")) {
            while (result.next()) {
                warehouses.add(result.getString(1));
            }
        }
        return warehouses;
    }

    public static int countActiveDeliveries() throws SQLException {
        ensureSchemaAndSeedData();
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT COUNT(*) FROM inventory_WarehouseDeliveries
                     WHERE status IN ('Scheduled', 'In Transit')
                     """)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    public static int createRandomDelivery() throws SQLException {
        ensureSchemaAndSeedData();
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int deliveryId = createRandomDelivery(connection);
                connection.commit();
                return deliveryId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Advances one random delivery and creates another when the queue gets short. */
    public static void runRandomUpdate() throws SQLException {
        ensureSchemaAndSeedData();
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int active = activeDeliveryCount(connection);
                if (active < 4) {
                    createRandomDelivery(connection);
                } else {
                    advanceRandomDelivery(connection);
                }
                if (activeDeliveryCount(connection) < 3) {
                    createRandomDelivery(connection);
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static WarehouseAssignment resolveWarehouse(String deliveryAddress)
            throws SQLException {
        ensureSchemaAndSeedData();
        try (Connection connection = DBConnection.getConnection()) {
            return resolveWarehouse(connection, deliveryAddress);
        }
    }

    public static WarehouseAssignment resolveWarehouse(
            Connection connection, String deliveryAddress) throws SQLException {
        String address = normalise(deliveryAddress);
        WarehouseAssignment fallback = null;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT warehouseID, name, serviceArea, addressAliases
                     FROM inventory_Warehouses ORDER BY warehouseID
                     """)) {
            while (result.next()) {
                WarehouseAssignment warehouse = new WarehouseAssignment(
                        result.getString("warehouseID"),
                        result.getString("name"),
                        result.getString("serviceArea"));
                if (fallback == null) {
                    fallback = warehouse;
                }
                String aliases = result.getString("addressAliases");
                if (!address.isEmpty() && aliases != null) {
                    for (String alias : aliases.split(",")) {
                        if (!alias.isBlank() && address.contains(normalise(alias))) {
                            return warehouse;
                        }
                    }
                }
            }
        }
        if (fallback == null) {
            throw new SQLException("No inventory warehouses are configured.");
        }
        return fallback;
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS inventory_Warehouses (
                        warehouseID TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        serviceArea TEXT NOT NULL,
                        addressAliases TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS inventory_WarehouseDeliveries (
                        deliveryID INTEGER PRIMARY KEY AUTOINCREMENT,
                        warehouseID TEXT NOT NULL,
                        ingredientID INTEGER NOT NULL,
                        quantity INTEGER NOT NULL CHECK(quantity > 0),
                        status TEXT NOT NULL DEFAULT 'Scheduled'
                          CHECK(status IN ('Scheduled', 'In Transit', 'Delivered')),
                        expectedAt TEXT NOT NULL,
                        createdAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updatedAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (warehouseID) REFERENCES inventory_Warehouses(warehouseID)
                          ON UPDATE CASCADE ON DELETE RESTRICT,
                        FOREIGN KEY (ingredientID) REFERENCES product_Ingredient(ingredientID)
                          ON UPDATE CASCADE ON DELETE RESTRICT
                    )
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS inventory_idx_delivery_status
                    ON inventory_WarehouseDeliveries(status, updatedAt)
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS inventory_idx_stock_location
                    ON inventory_Stock(stockYear, warehouseID, ingredientID)
                    """);
        }
    }

    private static void seedWarehouses(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO inventory_Warehouses
                  (warehouseID, name, serviceArea, addressAliases)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(warehouseID) DO UPDATE SET
                  name=excluded.name,
                  serviceArea=excluded.serviceArea,
                  addressAliases=excluded.addressAliases
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String[] warehouse : WAREHOUSES) {
                for (int index = 0; index < warehouse.length; index++) {
                    statement.setString(index + 1, warehouse[index]);
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void seedLocationStock(Connection connection) throws SQLException {
        String sql = """
                INSERT INTO inventory_Stock
                  (stockYear, stockCode, ingredientID, stockQuantity, warehouseID, capacity)
                SELECT ?,
                       printf('LOC-%s-%03d', replace(warehouse.warehouseID, '-', ''),
                              ingredient.ingredientID),
                       ingredient.ingredientID,
                       CASE
                         WHEN ((ingredient.ingredientID
                               + CAST(substr(warehouse.warehouseID, 4) AS INTEGER) * 3) % 29) = 0
                           THEN 0
                         ELSE 150 + ((ingredient.ingredientID * 37
                               + CAST(substr(warehouse.warehouseID, 4) AS INTEGER) * 53) % 650)
                       END,
                       warehouse.warehouseID,
                       1000
                FROM inventory_Warehouses warehouse
                CROSS JOIN product_Ingredient ingredient
                WHERE NOT EXISTS (
                    SELECT 1 FROM inventory_Stock stock
                    WHERE stock.stockYear = ?
                      AND stock.ingredientID = ingredient.ingredientID
                      AND UPPER(TRIM(stock.warehouseID)) = warehouse.warehouseID
                )
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            statement.setInt(2, CURRENT_STOCK_YEAR);
            statement.executeUpdate();
        }
    }

    private static void seedInitialDeliveries(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM inventory_WarehouseDeliveries")) {
            if (result.next() && result.getInt(1) > 0) {
                return;
            }
        }
        for (int index = 0; index < 5; index++) {
            createRandomDelivery(connection);
        }
    }

    private static int createRandomDelivery(Connection connection) throws SQLException {
        String candidateSql = """
                SELECT stock.warehouseID, stock.ingredientID,
                       stock.stockQuantity, stock.capacity
                FROM inventory_Stock stock
                WHERE stock.stockYear = ?
                  AND stock.stockQuantity < stock.capacity
                  AND NOT EXISTS (
                    SELECT 1 FROM inventory_WarehouseDeliveries delivery
                    WHERE delivery.warehouseID = stock.warehouseID
                      AND delivery.ingredientID = stock.ingredientID
                      AND delivery.status IN ('Scheduled', 'In Transit')
                  )
                ORDER BY (stock.stockQuantity * 1.0 / MAX(1, stock.capacity)) ASC,
                         RANDOM()
                LIMIT 1
                """;
        String warehouse;
        int ingredientId;
        int quantity;
        try (PreparedStatement statement = connection.prepareStatement(candidateSql)) {
            statement.setInt(1, CURRENT_STOCK_YEAR);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Every warehouse ingredient is already fully stocked.");
                }
                warehouse = result.getString("warehouseID");
                ingredientId = result.getInt("ingredientID");
                int availableSpace = result.getInt("capacity")
                        - result.getInt("stockQuantity");
                int requested = ThreadLocalRandom.current().nextInt(80, 251);
                quantity = Math.max(1, Math.min(availableSpace, requested));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expected = now.plusMinutes(
                ThreadLocalRandom.current().nextInt(2, 8));
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO inventory_WarehouseDeliveries
                  (warehouseID, ingredientID, quantity, status,
                   expectedAt, createdAt, updatedAt)
                VALUES (?, ?, ?, 'Scheduled', ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, warehouse);
            statement.setInt(2, ingredientId);
            statement.setInt(3, quantity);
            statement.setString(4, expected.format(DATABASE_TIME));
            statement.setString(5, now.format(DATABASE_TIME));
            statement.setString(6, now.format(DATABASE_TIME));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private static void advanceRandomDelivery(Connection connection) throws SQLException {
        int deliveryId;
        String warehouse;
        int ingredientId;
        int quantity;
        String status;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT deliveryID, warehouseID, ingredientID, quantity, status
                     FROM inventory_WarehouseDeliveries
                     WHERE status IN ('Scheduled', 'In Transit')
                     ORDER BY RANDOM() LIMIT 1
                     """)) {
            if (!result.next()) {
                createRandomDelivery(connection);
                return;
            }
            deliveryId = result.getInt("deliveryID");
            warehouse = result.getString("warehouseID");
            ingredientId = result.getInt("ingredientID");
            quantity = result.getInt("quantity");
            status = result.getString("status");
        }

        String now = LocalDateTime.now().format(DATABASE_TIME);
        if ("Scheduled".equals(status)) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE inventory_WarehouseDeliveries
                    SET status='In Transit', updatedAt=? WHERE deliveryID=?
                    """)) {
                statement.setString(1, now);
                statement.setInt(2, deliveryId);
                statement.executeUpdate();
            }
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE inventory_Stock
                SET stockQuantity = MIN(capacity, stockQuantity + ?)
                WHERE stockYear=? AND ingredientID=?
                  AND UPPER(TRIM(warehouseID))=?
                """)) {
            statement.setInt(1, quantity);
            statement.setInt(2, CURRENT_STOCK_YEAR);
            statement.setInt(3, ingredientId);
            statement.setString(4, warehouse);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Delivery destination stock row is missing.");
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE inventory_WarehouseDeliveries
                SET status='Delivered', updatedAt=? WHERE deliveryID=?
                """)) {
            statement.setString(1, now);
            statement.setInt(2, deliveryId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO inventory_stock_TransactionLog
                  (username, action, details, dateTime)
                VALUES ('Delivery System', 'WAREHOUSE_DELIVERY', ?, ?)
                """)) {
            statement.setString(1, "Delivery #" + deliveryId + " added " + quantity
                    + " units of ingredient #" + ingredientId + " to " + warehouse);
            statement.setString(2, now);
            statement.executeUpdate();
        }
    }

    private static int activeDeliveryCount(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                     SELECT COUNT(*) FROM inventory_WarehouseDeliveries
                     WHERE status IN ('Scheduled', 'In Transit')
                     """)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static final class WarehouseAssignment {
        private final String warehouseID;
        private final String warehouseName;
        private final String serviceArea;

        private WarehouseAssignment(
                String warehouseID, String warehouseName, String serviceArea) {
            this.warehouseID = warehouseID;
            this.warehouseName = warehouseName;
            this.serviceArea = serviceArea;
        }

        public String getWarehouseID() { return warehouseID; }
        public String getWarehouseName() { return warehouseName; }
        public String getServiceArea() { return serviceArea; }

        public String getDisplayName() {
            return warehouseID + " - " + serviceArea;
        }
    }
}
