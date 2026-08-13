# Orders Module

Manages the full lifecycle of a customer order in the Loop app: building a
cart from the menu, placing an order, tracking its status, and cancelling it
when appropriate.

## Structure

```
src/main/java/orders/
    App.java                - JavaFX entry point (used when running standalone)
    Launcher.java            - Launch helper; works around an Eclipse/JavaFX
                                module-path issue when App is run directly
    MenuItem.java             - A sample menu item (id, name, price)
    CartLine.java              - One line in the in-memory cart
    CartStore.java            - Simple in-memory cart (merges repeat adds of
                                the same item into one line)
    OrderStatus.java          - Order lifecycle: Pending, Confirmed,
                                Preparing, Out For Delivery, Delivered,
                                Cancelled
    OrderLineItem.java        - A snapshot of one cart line at order time
    Order.java                 - A full order and its line items
    OrderRepository.java   - All SQLite database access: schema creation,
                                seeding, placing/updating/cancelling orders
    OrdersController.java  - FXML controller for the Orders screen

src/main/resources/orders/
    Orders.fxml                - UI layout for the Orders screen
    styles.css                 - Stylesheet matching the app's colour theme

src/test/java/orders/
    OrderStatusTest.java     - Unit tests for order status transitions
    OrderTest.java             - Unit tests for order/line-item calculations
    CartStoreTest.java        - Unit tests for the in-memory cart
```

## Running standalone

```
mvn clean javafx:run
```

Opens directly into the Orders screen with a small seeded sample menu.
Orders are persisted to `database/OrdersDatabase.db`, created automatically
on first run.

## Running the tests

```
mvn test
```

## Integration with the Team Hub

When run as part of the full Loop app (`modules/product`), this module's
`OrdersController` is loaded via FXML by the Team Hub, and the "Loop" text
label doubles as the back-to-hub control (handled by the hub's
`HubNavigation` class, not by this module).
