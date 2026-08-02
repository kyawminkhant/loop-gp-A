package ProductPage.ProductPage;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TeamHubController {

    public Label statusLabel;

    public void initialize() {
        statusLabel.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> newScene.getRoot()
                        .lookupAll(".component-card")
                        .forEach(this::animateComponentCard));
            }
        });
    }

    public void openProducts(ActionEvent event) {
        openFxml(event, "Product Menu");
    }

    public void openCustomers(ActionEvent event) {
        launch("customer", "login", "Customers");
    }

    public void openOrders(ActionEvent event) {
        launch("orders", "orders", "Orders");
    }

    public void openDelivery(ActionEvent event) {
        openModuleMenu(event, "Delivery & Logistics", "Choose an operational role", "delivery",
                "Admin Workspace", "Manage deliveries and assign drivers", "admin",
                "Driver Workspace", "Accept deliveries and view routes", "driver");
    }

    public void openInventory(ActionEvent event) {
        launch("inventory", "dashboard", "Inventory / Warehousing");
    }

    public void openReviews(ActionEvent event) {
        openModuleMenu(event, "Reviews & Ratings", "Choose a customer or admin view", "reviews",
                "Customer View", "Browse products and submit reviews", "customer",
                "Admin Moderation", "Moderate reviews and view analytics", "admin");
    }

    public void openFinance(ActionEvent event) {
        launch("finance", "finance-dashboard", "Finance & Reporting");
    }

    private void launch(String module, String view, String componentName) {
        try {
            ModuleLauncher.launch(module, view);
            statusLabel.setText("Opening " + componentName + "...");
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not launch " + componentName + ": " + ex.getMessage());
        }
    }

    private void openModuleMenu(ActionEvent event, String title, String subtitle, String module,
            String firstTitle, String firstCaption, String firstView,
            String secondTitle, String secondCaption, String secondView) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("Module Menu.fxml"));
            Parent root = loader.load();
            ModuleMenuController controller = loader.getController();
            controller.configure(title, subtitle, module, firstTitle, firstCaption, firstView,
                    secondTitle, secondCaption, secondView);
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not open the " + title + " menu.");
        }
    }

    private void openFxml(ActionEvent event, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlName + ".fxml"));
            Parent root = loader.load();

            if ("Main Page".equals(fxmlName)) {
                PrimaryController controller = loader.getController();
                controller.setFirstName(shortName("Jasper"));
            }

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not open " + fxmlName + ".fxml. Check the file name and controller.");
        }
    }

    private String shortName(String name) {
        if (name == null || name.isBlank()) {
            return "Guest";
        }

        String firstName = name.contains(" ") ? name.split(" ")[0] : name;
        if (firstName.length() > 8) {
            return firstName.substring(0, 8) + "...";
        }
        return firstName;
    }

    private void animateComponentCard(Node card) {
        card.setOpacity(0);
        card.setTranslateY(24);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(420), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(420), card);
        slideIn.setFromY(24);
        slideIn.setToY(0);

        fadeIn.play();
        slideIn.play();

        card.setOnMouseEntered(event -> scale(card, 1.045));
        card.setOnMouseExited(event -> scale(card, 1.0));
    }

    private void scale(Node node, double value) {
        Object oldAnimation = node.getProperties().get("hubScaleAnimation");
        if (oldAnimation instanceof ScaleTransition) {
            ((ScaleTransition) oldAnimation).stop();
        }

        ScaleTransition scale = new ScaleTransition(Duration.millis(140), node);
        scale.setToX(value);
        scale.setToY(value);
        scale.setOnFinished(event -> {
            node.setScaleX(value);
            node.setScaleY(value);
            node.getProperties().remove("hubScaleAnimation");
        });
        node.getProperties().put("hubScaleAnimation", scale);
        scale.play();
    }
}
