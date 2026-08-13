-- Adds the reusable customer used to demonstrate the 500-character review rule.
-- The five Reviews purchases are intentionally left without matching reviews.

PRAGMA foreign_keys = ON;
BEGIN IMMEDIATE;

INSERT OR IGNORE INTO customer_People
    (personID, name, email, mobile, passwordHash)
VALUES
    ('demo-person-008', 'Alex Morgan', 'review.demo@loop.demo', '07123456796',
     'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791');

INSERT OR IGNORE INTO customer_Customers
    (customerID, personID, deliveryAddress, idCardNo, status)
VALUES
    ('demo-customer-008', 'demo-person-008', '18 Demo Lane, London',
     'LOOP-DEMO-008', 'Active');

INSERT OR IGNORE INTO customer_CustomerPreference
    (preferenceID, customerID, favoriteCategories,
     notificationSettings, deliveryInstructions)
VALUES
    ('demo-preference-008', 'demo-customer-008', 'General', 'Enabled', '');

INSERT INTO orders_Orders
    (customerID, customerName, orderDate, status, totalAmount)
SELECT 'demo-customer-008', 'Alex Morgan', '2026-08-01 18:30:00',
       'Delivered', product.price
FROM product_Products product
WHERE product.productName = 'Vegetable Gyoza'
  AND NOT EXISTS (
      SELECT 1 FROM orders_Orders existing
      WHERE existing.customerID = 'demo-customer-008'
        AND existing.orderDate = '2026-08-01 18:30:00'
  );

INSERT INTO orders_OrderItems
    (orderID, productID, itemName, quantity, priceAtOrder)
SELECT customer_order.orderID, product.productID, product.productName, 1, product.price
FROM orders_Orders customer_order
JOIN product_Products product ON product.productName = 'Vegetable Gyoza'
WHERE customer_order.customerID = 'demo-customer-008'
  AND customer_order.orderDate = '2026-08-01 18:30:00'
  AND NOT EXISTS (
      SELECT 1 FROM orders_OrderItems existing
      WHERE existing.orderID = customer_order.orderID
        AND existing.productID = product.productID
  );

INSERT OR IGNORE INTO reviews_users
    (name, email, password, role, address)
VALUES
    ('Alex Morgan', 'review.demo@loop.demo', 'demo123', 'CUSTOMER',
     '18 Demo Lane, London');

INSERT INTO reviews_orders (customer_id, product_id, order_date)
SELECT review_user.id, product.productID, '2026-08-01'
FROM reviews_users review_user
CROSS JOIN product_Products product
WHERE review_user.email = 'review.demo@loop.demo'
  AND product.status = 1
  AND product.productName IN (
      'Vegetable Gyoza', 'Beef Bulgogi', 'Thai Green Curry',
      'Falafel Mezze Bowl', 'Mediterranean Salmon'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM reviews_orders existing
      WHERE existing.customer_id = review_user.id
        AND existing.product_id = product.productID
  );

COMMIT;
