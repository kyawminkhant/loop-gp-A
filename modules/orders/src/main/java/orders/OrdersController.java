package orders;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class OrdersController {

    private static final String FILTER_ALL = "All Statuses";

    @FXML private VBox menuItemsBox;
    @FXML private VBox cartLinesBox;
    @FXML private VBox orderRows;
    @FXML private Label cartTotalLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label statusLabel;
    @FXML private TextField customerNameField;
    @FXML private ComboBox<String> statusFilterCombo;

    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
    private List<Order> allOrders = List.of();

    @FXML
    public void initialize() {
        statusFilterCombo.getItems().add(FILTER_ALL);
        for (OrderStatus status : OrderStatus.values()) {
            statusFilterCombo.getItems().add(status.label());
        }
        statusFilterCombo.getSelectionModel().select(FILTER_ALL);
        statusFilterCombo.setOnAction(event -> renderOrders());

        try {
            OrderRepository.ensureSchema();
            renderMenu(OrderRepository.getMenuItems());
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not prepare the database.");
        }

        refreshCartDisplay();
        loadOrders();
    }

    
    private void renderMenu(List<MenuItem> menuItems) {
        menuItemsBox.getChildren().clear();

        for (MenuItem item : menuItems) {
            HBox row = new HBox(10);
            row.getStyleClass().add("menu-row");
            row.setMaxWidth(Double.MAX_VALUE);

            Label name = new Label(item.name);
            name.getStyleClass().add("menu-item-name");
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);

            Label price = new Label(moneyFormat.format(item.price));
            price.getStyleClass().add("menu-item-price");

            Spinner<Integer> quantitySpinner = new Spinner<>();
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
            quantitySpinner.setEditable(true);
            quantitySpinner.getStyleClass().add("quantity-spinner");
            quantitySpinner.setPrefWidth(70);

            Button addButton = new Button("Add");
            addButton.getStyleClass().add("add-to-cart-button");
            addButton.setOnAction(event -> {
                int quantity = quantitySpinner.getValue();
                CartStore.addItem(item, quantity);
                refreshCartDisplay();
                statusLabel.setText(quantity + "x " + item.name + " added to cart.");
            });

            row.getChildren().addAll(name, price, quantitySpinner, addButton);
            menuItemsBox.getChildren().add(row);
        }
    }

    private void refreshCartDisplay() {
        cartLinesBox.getChildren().clear();

        List<CartLine> lines = CartStore.getLines();
        if (lines.isEmpty()) {
            Label empty = new Label("Cart is empty.");
            empty.getStyleClass().add("cart-empty-label");
            cartLinesBox.getChildren().add(empty);
        } else {
            for (CartLine line : lines) {
                HBox row = new HBox(8);
                row.getStyleClass().add("cart-line-row");

                Label text = new Label(line.getQuantity() + "x " + line.itemName);
                text.getStyleClass().add("cart-line-text");
                text.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(text, Priority.ALWAYS);

                Label lineTotal = new Label(moneyFormat.format(line.lineTotal()));
                lineTotal.getStyleClass().add("cart-line-total");

                row.getChildren().addAll(text, lineTotal);
                cartLinesBox.getChildren().add(row);
            }
        }

        cartTotalLabel.setText("Cart Total: " + moneyFormat.format(CartStore.getTotal()));
    }

    @FXML
    public void clearCart(ActionEvent event) {
        CartStore.clear();
        refreshCartDisplay();
        statusLabel.setText("Cart cleared.");
    }

    @FXML
    public void placeOrderFromCart(ActionEvent event) {
        String name = customerNameField.getText();
        try {
            int orderId = OrderRepository.placeOrderFromCart(name);
            statusLabel.setText("Order #" + orderId + " placed for "
                    + (name == null || name.isBlank() ? "Guest" : name.trim()) + ".");
            customerNameField.clear();
            refreshCartDisplay();
            loadOrders();
        } catch (IllegalStateException exception) {
            statusLabel.setText(exception.getMessage());
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not place the order. Please try again.");
        }
    }

    

    @FXML
    public void refreshOrders(ActionEvent event) {
        loadOrders();
        statusLabel.setText("Orders refreshed.");
    }

    private void loadOrders() {
        try {
            allOrders = OrderRepository.getAllOrders();
            renderOrders();
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not load orders_Orders from the database.");
        }
    }

    private void renderOrders() {
        orderRows.getChildren().clear();

        String filter = statusFilterCombo.getValue();
        List<Order> visible = allOrders.stream()
                .filter(order -> FILTER_ALL.equals(filter) || order.status.label().equals(filter))
                .collect(Collectors.toList());

        totalOrdersLabel.setText("Total Orders: " + visible.size());

        if (visible.isEmpty()) {
            Label empty = new Label("No orders found.");
            empty.getStyleClass().add("orders-empty-label");
            orderRows.getChildren().add(empty);
            return;
        }

        for (Order order : visible) {
            orderRows.getChildren().add(createOrderRow(order));
        }
    }

    private HBox createOrderRow(Order order) {
        HBox row = new HBox(16);
        row.getStyleClass().add("order-row");
        row.setMaxWidth(Double.MAX_VALUE);

        Label id = new Label("#" + order.orderId);
        id.getStyleClass().add("order-col-id");

        Label customer = new Label(order.customerName);
        customer.getStyleClass().add("order-col-customer");
        customer.setWrapText(true);

        Label items = new Label(order.itemsSummary());
        items.getStyleClass().add("order-col-items");
        items.setWrapText(true);
        items.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(items, Priority.ALWAYS);

        Label total = new Label(moneyFormat.format(order.totalAmount));
        total.getStyleClass().add("order-col-money");

        Label date = new Label(order.orderDate);
        date.getStyleClass().add("order-col-date");

        Label statusPill = new Label(order.status.label());
        statusPill.getStyleClass().addAll("order-status-pill", order.status.styleClass());

        ComboBox<OrderStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(OrderStatus.values());
        statusCombo.getSelectionModel().select(order.status);
        statusCombo.getStyleClass().add("order-status-combo");
        statusCombo.setDisable(order.status.isFinal());
        statusCombo.setOnAction(event -> changeStatus(order, statusCombo.getValue()));

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("order-cancel-button");
        cancelButton.setDisable(order.status.isFinal());
        cancelButton.setOnAction(event -> cancelOrder(order));

        HBox actionBox = new HBox(8, statusCombo, cancelButton);
        actionBox.getStyleClass().add("order-col-action");

        row.getChildren().addAll(id, customer, items, total, date, statusPill, actionBox);
        return row;
    }

    private void changeStatus(Order order, OrderStatus newStatus) {
        if (newStatus == null || newStatus == order.status) {
            return;
        }
        try {
            OrderRepository.updateStatus(order.orderId, newStatus);
            statusLabel.setText("Order #" + order.orderId + " is now " + newStatus.label() + ".");
            loadOrders();
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not update order #" + order.orderId + ".");
        }
    }

    private void cancelOrder(Order order) {
        try {
            boolean cancelled = OrderRepository.cancelOrder(order.orderId);
            statusLabel.setText(cancelled
                    ? "Order #" + order.orderId + " was cancelled."
                    : "Order #" + order.orderId + " could not be cancelled (already " + order.status.label() + ").");
            loadOrders();
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            statusLabel.setText("Could not cancel order #" + order.orderId + ".");
        }
    }
}
