# Simplified shared database schema

All components use `database/loop.db`. The database contains 28 real tables,
down from 53, plus four compatibility views.

## Shared Product catalogue

These tables are the single source of truth for Products, Orders, Inventory,
Finance reporting, and Reviews:

- `product_Products`
- `product_Chef`
- `product_ChefReview`
- `product_Category`
- `product_Ingredient`
- `product_DefaultIngredient`
- `product_ProductImage`
- `product_ProductIngredientOption`
- `product_Ratings`

`product_Products.sourceModule` distinguishes normal catalogue products from
Finance or Reviews demonstration records. `product_Ingredient.imagePath` now
stores the image used by Inventory.

## Shared Orders

- `orders_Orders`
- `orders_OrderItems`

`orders_OrderItems.productID` references `product_Products.productID`.
`orders_Orders.customerID` can reference `customer_Customers.customerID`.
The former `orders_MenuItems` and `customer_OrderHistoryDummy` copies are no
longer needed.

## Customer

- `customer_People`
- `customer_Customers`
- `customer_CustomerPreference`
- `customer_Chefs`
- `customer_ChefReviews`

Customer order history is read from the shared `orders_Orders` table.

## Inventory

- `inventory_Stock`
- `inventory_stock_analytics`
- `inventory_stock_moreStorageLocations`
- `inventory_stock_TransactionLog`

`inventory_Stock.stockYear` replaces the five separate yearly ingredient
tables. Its `ingredientID` references the shared `product_Ingredient` table.

## Delivery

- `delivery_AllDeliveries`
- `delivery_DeliveryDetails`

## Finance

- `finance_Users`

Finance reads the shared Product and Order data through these compatibility
views, so its reporting code remains independent without copying rows:

- `finance_Product`
- `finance_Orders`
- `finance_OrderItem`

## Reviews

- `reviews_users`
- `reviews_orders`
- `reviews_reviews`
- `reviews_helpful_votes`
- `reviews_admin_moderation_log`

`reviews_orders.product_id` and `reviews_reviews.product_id` reference the
shared `product_Products` table. `reviews_products` is now a view of the shared
catalogue rather than another Product table.

## Migration

The one-time migration used to preserve and consolidate the original data is
stored in `database/migrations/001_simplify_shared_schema.sql`.
