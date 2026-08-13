-- Adds service-area warehouses and live inbound deliveries.
-- Existing historical stock years and stock quantities are preserved.

PRAGMA foreign_keys = ON;
BEGIN IMMEDIATE;

CREATE TABLE IF NOT EXISTS inventory_Warehouses (
    warehouseID TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    serviceArea TEXT NOT NULL,
    addressAliases TEXT NOT NULL
);

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
);

CREATE INDEX IF NOT EXISTS inventory_idx_delivery_status
ON inventory_WarehouseDeliveries(status, updatedAt);

CREATE INDEX IF NOT EXISTS inventory_idx_stock_location
ON inventory_Stock(stockYear, warehouseID, ingredientID);

INSERT OR REPLACE INTO inventory_Warehouses VALUES
('WH-01','Central Kitchen Warehouse','Central London','london,paddington,kensington,westminster,camden'),
('WH-02','Kingston Warehouse','Kingston','kingston,surbiton,new malden'),
('WH-03','Richmond Warehouse','Richmond','richmond,kew,twickenham'),
('WH-04','Hounslow Warehouse','Hounslow','hounslow,feltham,hayes'),
('WH-05','Croydon Warehouse','Croydon','croydon,purley,thornton heath'),
('WH-06','Wimbledon Warehouse','Wimbledon','wimbledon,merton,mitcham'),
('WH-07','Barnet Warehouse','Barnet','barnet,enfield,finchley'),
('WH-08','Harrow Warehouse','Harrow','harrow,greenford,ruislip'),
('WH-09','Romford Warehouse','Romford','romford,ilford,barking'),
('WH-10','Wembley Warehouse','Wembley','wembley,brent,hillingdon');

INSERT INTO inventory_Stock
  (stockYear, stockCode, ingredientID, stockQuantity, warehouseID, capacity)
SELECT 2026,
       printf('LOC-%s-%03d', replace(warehouse.warehouseID, '-', ''), ingredient.ingredientID),
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
    WHERE stock.stockYear = 2026
      AND stock.ingredientID = ingredient.ingredientID
      AND UPPER(TRIM(stock.warehouseID)) = warehouse.warehouseID
);

INSERT INTO inventory_WarehouseDeliveries
  (warehouseID, ingredientID, quantity, status, expectedAt, createdAt, updatedAt)
SELECT stock.warehouseID, stock.ingredientID, 200, 'Scheduled',
       datetime('now', '+' || (row_number() OVER (ORDER BY stock.warehouseID,
                 stock.ingredientID) + 2) || ' minutes'),
       datetime('now'), datetime('now')
FROM inventory_Stock stock
WHERE stock.stockYear = 2026
  AND stock.stockQuantity = 0
  AND NOT EXISTS (SELECT 1 FROM inventory_WarehouseDeliveries)
ORDER BY stock.warehouseID, stock.ingredientID
LIMIT 5;

COMMIT;
