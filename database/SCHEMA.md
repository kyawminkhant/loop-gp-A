# Merged database schema

All tables live in `database/loop.db` and are separated by domain prefix.

## customer

- `customer_ChefReviews`
- `customer_Chefs`
- `customer_CustomerPreference`
- `customer_Customers`
- `customer_OrderHistoryDummy`
- `customer_People`

## delivery

- `delivery_AllDeliveries`
- `delivery_DeliveryDetails`

## finance

- `finance_OrderItem`
- `finance_Orders`
- `finance_Product`
- `finance_Users`

## inventory

- `inventory_product_Category`
- `inventory_product_Chef`
- `inventory_product_ChefReview`
- `inventory_product_DefaultIngredient`
- `inventory_product_Ingredient`
- `inventory_product_Ingredient2022`
- `inventory_product_Ingredient2023`
- `inventory_product_Ingredient2024`
- `inventory_product_Ingredient2025`
- `inventory_product_Ingredient2026`
- `inventory_product_ProductImage`
- `inventory_product_ProductIngredientOption`
- `inventory_product_Products`
- `inventory_product_Ratings`
- `inventory_stock_analytics`
- `inventory_stock_Ingredient2022`
- `inventory_stock_Ingredient2023`
- `inventory_stock_Ingredient2024`
- `inventory_stock_Ingredient2025`
- `inventory_stock_Ingredient2026`
- `inventory_stock_moreStorageLocations`
- `inventory_stock_TransactionLog`
- `inventory_stock_UserAccounts`

## orders

- `orders_MenuItems`
- `orders_OrderItems`
- `orders_Orders`

## product

- `product_Category`
- `product_Chef`
- `product_ChefReview`
- `product_DefaultIngredient`
- `product_Ingredient`
- `product_ProductImage`
- `product_ProductIngredientOption`
- `product_Products`
- `product_Ratings`

## reviews

- `reviews_admin_moderation_log`
- `reviews_helpful_votes`
- `reviews_orders`
- `reviews_products`
- `reviews_reviews`
- `reviews_users`
