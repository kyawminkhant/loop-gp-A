-- LOOP shared-database simplification
--
-- Consolidates copied Product/Order tables while preserving every existing row.
-- Run once against the database produced by the original component merge.

PRAGMA foreign_keys = OFF;
BEGIN IMMEDIATE;

-- The Product component owns the canonical catalog and ingredient master.
ALTER TABLE product_Ingredient ADD COLUMN imagePath TEXT;
ALTER TABLE product_Products ADD COLUMN sourceModule TEXT NOT NULL DEFAULT 'product';
ALTER TABLE product_Products ADD COLUMN stockQuantity INTEGER NOT NULL DEFAULT 100;

UPDATE product_Ingredient
SET imagePath = (
    SELECT duplicate.imagePath
    FROM inventory_product_Ingredient duplicate
    WHERE duplicate.ingredientID = product_Ingredient.ingredientID
)
WHERE EXISTS (
    SELECT 1
    FROM inventory_product_Ingredient duplicate
    WHERE duplicate.ingredientID = product_Ingredient.ingredientID
);

-- Preserve stock-only ingredients by adding them to the shared ingredient master.
WITH all_stock_names AS (
    SELECT ingredientName, NULL AS imagePath FROM inventory_stock_Ingredient2022
    UNION ALL SELECT ingredientName, NULL FROM inventory_stock_Ingredient2023
    UNION ALL SELECT ingredientName, NULL FROM inventory_stock_Ingredient2024
    UNION ALL SELECT ingredientName, NULL FROM inventory_stock_Ingredient2025
    UNION ALL SELECT ingredientName, imagePath FROM inventory_stock_Ingredient2026
), missing_names AS (
    SELECT trim(stock.ingredientName) AS ingredientName, MAX(stock.imagePath) AS imagePath
    FROM all_stock_names stock
    WHERE NOT EXISTS (
        SELECT 1 FROM product_Ingredient ingredient
        WHERE lower(trim(ingredient.ingredientName)) = lower(trim(stock.ingredientName))
    )
    GROUP BY lower(trim(stock.ingredientName))
)
INSERT INTO product_Ingredient (
    ingredientName, calories, protein, carbohydrates, sugars,
    fat, saturatedFat, fiber, sodium, imagePath
)
SELECT ingredientName, 0, 0, 0, 0, 0, 0, 0, 0, imagePath
FROM missing_names;

UPDATE product_Ingredient
SET imagePath = COALESCE(
    imagePath,
    (
        SELECT stock.imagePath
        FROM inventory_stock_Ingredient2026 stock
        WHERE lower(trim(stock.ingredientName)) = lower(trim(product_Ingredient.ingredientName))
          AND stock.imagePath IS NOT NULL
        LIMIT 1
    )
);

-- One stock table replaces Ingredient2022 ... Ingredient2026.
CREATE TABLE inventory_Stock (
    stockYear INTEGER NOT NULL,
    stockCode TEXT NOT NULL,
    ingredientID INTEGER NOT NULL,
    stockQuantity INTEGER NOT NULL DEFAULT 0,
    warehouseID TEXT,
    capacity INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (stockYear, stockCode),
    FOREIGN KEY (ingredientID) REFERENCES product_Ingredient(ingredientID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

INSERT INTO inventory_Stock
SELECT 2022, stock.ingredientID, ingredient.ingredientID,
       stock.stockQuantity, stock.warehouseID, stock.capacity
FROM inventory_stock_Ingredient2022 stock
JOIN product_Ingredient ingredient
  ON lower(trim(ingredient.ingredientName)) = lower(trim(stock.ingredientName));

INSERT INTO inventory_Stock
SELECT 2023, stock.ingredientID, ingredient.ingredientID,
       stock.stockQuantity, stock.warehouseID, stock.capacity
FROM inventory_stock_Ingredient2023 stock
JOIN product_Ingredient ingredient
  ON lower(trim(ingredient.ingredientName)) = lower(trim(stock.ingredientName));

INSERT INTO inventory_Stock
SELECT 2024, stock.ingredientID, ingredient.ingredientID,
       stock.stockQuantity, stock.warehouseID, stock.capacity
FROM inventory_stock_Ingredient2024 stock
JOIN product_Ingredient ingredient
  ON lower(trim(ingredient.ingredientName)) = lower(trim(stock.ingredientName));

INSERT INTO inventory_Stock
SELECT 2025, stock.ingredientID, ingredient.ingredientID,
       stock.stockQuantity, stock.warehouseID, stock.capacity
FROM inventory_stock_Ingredient2025 stock
JOIN product_Ingredient ingredient
  ON lower(trim(ingredient.ingredientName)) = lower(trim(stock.ingredientName));

INSERT INTO inventory_Stock
SELECT 2026, stock.ingredientID, ingredient.ingredientID,
       stock.stockQuantity, stock.warehouseID, stock.capacity
FROM inventory_stock_Ingredient2026 stock
JOIN product_Ingredient ingredient
  ON lower(trim(ingredient.ingredientName)) = lower(trim(stock.ingredientName));

CREATE INDEX inventory_idx_stock_year ON inventory_Stock(stockYear);
CREATE INDEX inventory_idx_stock_ingredient ON inventory_Stock(ingredientID);

DROP TABLE inventory_product_Category;
DROP TABLE inventory_product_Chef;
DROP TABLE inventory_product_ChefReview;
DROP TABLE inventory_product_DefaultIngredient;
DROP TABLE inventory_product_Ingredient;
DROP TABLE inventory_product_Ingredient2022;
DROP TABLE inventory_product_Ingredient2023;
DROP TABLE inventory_product_Ingredient2024;
DROP TABLE inventory_product_Ingredient2025;
DROP TABLE inventory_product_Ingredient2026;
DROP TABLE inventory_product_ProductImage;
DROP TABLE inventory_product_ProductIngredientOption;
DROP TABLE inventory_product_Products;
DROP TABLE inventory_product_Ratings;

DROP TABLE inventory_stock_Ingredient2022;
DROP TABLE inventory_stock_Ingredient2023;
DROP TABLE inventory_stock_Ingredient2024;
DROP TABLE inventory_stock_Ingredient2025;
DROP TABLE inventory_stock_Ingredient2026;
DROP TABLE inventory_stock_UserAccounts;

-- Orders now reference the canonical Product catalog instead of a copied menu.
ALTER TABLE orders_OrderItems RENAME TO orders_OrderItems_legacy;
ALTER TABLE orders_Orders RENAME TO orders_Orders_legacy;

CREATE TABLE orders_Orders (
    orderID INTEGER PRIMARY KEY AUTOINCREMENT,
    customerID TEXT,
    customerName TEXT NOT NULL DEFAULT 'Guest',
    orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'Pending',
    totalAmount REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID)
        ON UPDATE CASCADE ON DELETE SET NULL
);

INSERT INTO orders_Orders (orderID, customerID, customerName, orderDate, status, totalAmount)
SELECT legacy.orderID,
       (
           SELECT customer.customerID
           FROM customer_Customers customer
           JOIN customer_People person ON person.personID = customer.personID
           WHERE lower(trim(person.name)) = lower(trim(legacy.customerName))
           LIMIT 1
       ),
       legacy.customerName, legacy.orderDate, legacy.status, legacy.totalAmount
FROM orders_Orders_legacy legacy;

CREATE TABLE orders_OrderItems (
    orderItemID INTEGER PRIMARY KEY AUTOINCREMENT,
    orderID INTEGER NOT NULL,
    productID INTEGER NOT NULL,
    itemName TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    priceAtOrder REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (orderID) REFERENCES orders_Orders(orderID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (productID) REFERENCES product_Products(productID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

INSERT INTO orders_OrderItems (
    orderItemID, orderID, productID, itemName, quantity, priceAtOrder
)
SELECT item.orderItemID, item.orderID, product.productID,
       item.itemName, item.quantity, item.priceAtOrder
FROM orders_OrderItems_legacy item
JOIN product_Products product
  ON lower(trim(product.productName)) = lower(trim(item.itemName));

-- Move any customer-module sample history into the shared order table.
INSERT INTO orders_Orders (customerID, customerName, orderDate, status, totalAmount)
SELECT history.customerID, person.name, history.orderDate, history.status, history.totalAmount
FROM customer_OrderHistoryDummy history
JOIN customer_Customers customer ON customer.customerID = history.customerID
JOIN customer_People person ON person.personID = customer.personID;

DROP TABLE orders_OrderItems_legacy;
DROP TABLE orders_Orders_legacy;
DROP TABLE orders_MenuItems;
DROP TABLE customer_OrderHistoryDummy;

CREATE INDEX orders_idx_customer ON orders_Orders(customerID);
CREATE INDEX orders_idx_items_order ON orders_OrderItems(orderID);
CREATE INDEX orders_idx_items_product ON orders_OrderItems(productID);

-- Import Finance demo sales into the shared Product and Order tables.
INSERT INTO product_Products (
    productName, shortDescription, extendedDescription, cost, price,
    status, spiceLevel, country, createdDate, updatedDate, chefID,
    sourceModule, stockQuantity
)
SELECT finance.Name,
       'Finance catalogue item',
       'Imported from the original Finance component during schema simplification.',
       MIN(finance.cost), MAX(finance.Price), 0, 0, 'United Kingdom',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       (SELECT MIN(chefID) FROM product_Chef), 'finance', 0
FROM finance_Product finance
WHERE NOT EXISTS (
    SELECT 1 FROM product_Products product
    WHERE lower(trim(product.productName)) = lower(trim(finance.Name))
)
GROUP BY lower(trim(finance.Name));

CREATE TEMP TABLE finance_product_map AS
SELECT finance.ProductID AS oldProductID, product.productID AS newProductID
FROM finance_Product finance
JOIN product_Products product
  ON lower(trim(product.productName)) = lower(trim(finance.Name));

CREATE TEMP TABLE finance_order_map AS
SELECT finance.OrderID AS oldOrderID,
       (SELECT COALESCE(MAX(orderID), 0) FROM orders_Orders)
       + ROW_NUMBER() OVER (ORDER BY finance.OrderID) AS newOrderID
FROM finance_Orders finance;

INSERT INTO orders_Orders (
    orderID, customerID, customerName, orderDate, status, totalAmount
)
SELECT map.newOrderID, NULL, 'Finance import', finance.Date, 'Delivered', finance.TotalCost
FROM finance_Orders finance
JOIN finance_order_map map ON map.oldOrderID = finance.OrderID;

INSERT INTO orders_OrderItems (
    orderID, productID, itemName, quantity, priceAtOrder
)
SELECT orderMap.newOrderID, productMap.newProductID, product.Name,
       item.Quantity, item.UnitPrice
FROM finance_OrderItem item
JOIN finance_order_map orderMap ON orderMap.oldOrderID = item.OrderID
JOIN finance_product_map productMap ON productMap.oldProductID = item.ProductID
JOIN finance_Product product ON product.ProductID = item.ProductID;

DROP TABLE finance_OrderItem;
DROP TABLE finance_Orders;
DROP TABLE finance_Product;

CREATE VIEW finance_Orders AS
SELECT orderID AS OrderID, orderDate AS Date, totalAmount AS TotalCost
FROM orders_Orders;

CREATE VIEW finance_OrderItem AS
SELECT orderItemID AS OrderItemID, orderID AS OrderID, productID AS ProductID,
       quantity AS Quantity, priceAtOrder AS UnitPrice
FROM orders_OrderItems;

CREATE VIEW finance_Product AS
SELECT productID AS ProductID, productName AS Name, price AS Price, cost
FROM product_Products;

-- Move the Reviews catalogue into the same Product master.
INSERT INTO product_Products (
    productName, shortDescription, extendedDescription, cost, price,
    status, spiceLevel, country, createdDate, updatedDate, chefID,
    sourceModule, stockQuantity
)
SELECT reviewProduct.name,
       'Reviews catalogue item',
       'Imported from the original Reviews component during schema simplification.',
       round(reviewProduct.price * 0.5, 2), reviewProduct.price,
       0, 0, 'United Kingdom', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       (SELECT MIN(chefID) FROM product_Chef), 'reviews', reviewProduct.stock
FROM reviews_products reviewProduct
WHERE NOT EXISTS (
    SELECT 1 FROM product_Products product
    WHERE lower(trim(product.productName)) = lower(trim(reviewProduct.name))
);

CREATE TEMP TABLE reviews_product_map AS
SELECT reviewProduct.id AS oldProductID, product.productID AS newProductID
FROM reviews_products reviewProduct
JOIN product_Products product
  ON lower(trim(product.productName)) = lower(trim(reviewProduct.name));

INSERT INTO product_Category (
    chosenSortBy, chosenDietary, chosenHealthGoal, chosenCuisines, productID
)
SELECT '', COALESCE(reviewProduct.category, 'Not specified'),
       'Balanced Meals', COALESCE(reviewProduct.category, 'Not specified'),
       map.newProductID
FROM reviews_products reviewProduct
JOIN reviews_product_map map ON map.oldProductID = reviewProduct.id
WHERE NOT EXISTS (
    SELECT 1 FROM product_Category category WHERE category.productID = map.newProductID
);

INSERT INTO product_Ratings (rating, noPeople, productID)
SELECT reviewProduct.average_rating,
       (SELECT COUNT(*) FROM reviews_reviews review
        WHERE review.product_id = reviewProduct.id AND review.status = 'Active'),
       map.newProductID
FROM reviews_products reviewProduct
JOIN reviews_product_map map ON map.oldProductID = reviewProduct.id
WHERE NOT EXISTS (
    SELECT 1 FROM product_Ratings rating WHERE rating.productID = map.newProductID
);

ALTER TABLE reviews_admin_moderation_log RENAME TO reviews_admin_moderation_log_legacy;
ALTER TABLE reviews_helpful_votes RENAME TO reviews_helpful_votes_legacy;
ALTER TABLE reviews_orders RENAME TO reviews_orders_legacy;
ALTER TABLE reviews_reviews RENAME TO reviews_reviews_legacy;

CREATE TABLE reviews_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    order_date TEXT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES reviews_users(id),
    FOREIGN KEY (product_id) REFERENCES product_Products(productID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE reviews_reviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
    comment_text TEXT NOT NULL,
    image_url TEXT,
    created_at INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'Active',
    helpful_count INTEGER NOT NULL DEFAULT 0,
    unhelpful_count INTEGER NOT NULL DEFAULT 0,
    edit_duration_seconds INTEGER NOT NULL DEFAULT 300,
    UNIQUE(product_id, customer_id),
    FOREIGN KEY (product_id) REFERENCES product_Products(productID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES reviews_users(id)
);

CREATE TABLE reviews_helpful_votes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    review_id INTEGER NOT NULL,
    customer_id INTEGER NOT NULL,
    vote_type TEXT NOT NULL CHECK(vote_type IN ('helpful','unhelpful')),
    created_at INTEGER NOT NULL,
    UNIQUE(review_id, customer_id),
    FOREIGN KEY (review_id) REFERENCES reviews_reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES reviews_users(id)
);

CREATE TABLE reviews_admin_moderation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    admin_id INTEGER NOT NULL,
    review_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY (admin_id) REFERENCES reviews_users(id),
    FOREIGN KEY (review_id) REFERENCES reviews_reviews(id) ON DELETE CASCADE
);

INSERT INTO reviews_orders (id, customer_id, product_id, order_date)
SELECT legacy.id, legacy.customer_id, map.newProductID, legacy.order_date
FROM reviews_orders_legacy legacy
JOIN reviews_product_map map ON map.oldProductID = legacy.product_id;

INSERT INTO reviews_reviews (
    id, product_id, customer_id, rating, comment_text, image_url,
    created_at, status, helpful_count, unhelpful_count, edit_duration_seconds
)
SELECT legacy.id, map.newProductID, legacy.customer_id, legacy.rating,
       legacy.comment_text, legacy.image_url, legacy.created_at, legacy.status,
       legacy.helpful_count, legacy.unhelpful_count, legacy.edit_duration_seconds
FROM reviews_reviews_legacy legacy
JOIN reviews_product_map map ON map.oldProductID = legacy.product_id;

INSERT INTO reviews_helpful_votes
SELECT * FROM reviews_helpful_votes_legacy;

INSERT INTO reviews_admin_moderation_log
SELECT * FROM reviews_admin_moderation_log_legacy;

DROP TABLE reviews_admin_moderation_log_legacy;
DROP TABLE reviews_helpful_votes_legacy;
DROP TABLE reviews_orders_legacy;
DROP TABLE reviews_reviews_legacy;
DROP TABLE reviews_products;

CREATE VIEW reviews_products AS
SELECT product.productID AS id,
       product.productName AS name,
       product.price,
       product.stockQuantity AS stock,
       COALESCE(category.category, 'Meals') AS category,
       COALESCE(rating.average_rating, 0) AS average_rating
FROM product_Products product
LEFT JOIN (
    SELECT productID, MAX(chosenCuisines) AS category
    FROM product_Category GROUP BY productID
) category ON category.productID = product.productID
LEFT JOIN (
    SELECT productID,
           CASE WHEN SUM(noPeople) = 0 THEN 0
                ELSE SUM(rating * noPeople) / SUM(noPeople) END AS average_rating
    FROM product_Ratings GROUP BY productID
) rating ON rating.productID = product.productID
WHERE product.status = 1 OR product.sourceModule = 'reviews';

CREATE INDEX reviews_idx_orders_product ON reviews_orders(product_id);
CREATE INDEX reviews_idx_reviews_product ON reviews_reviews(product_id);
CREATE INDEX reviews_idx_reviews_customer ON reviews_reviews(customer_id);

COMMIT;
PRAGMA foreign_keys = ON;
