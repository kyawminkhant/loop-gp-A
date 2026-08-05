package ProductPage.ProductPage;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import models.Customer;
import utils.SessionManager;

/** Displays each independently maintained module in the Team Hub's current window. */
public final class ModuleLauncher {

    private ModuleLauncher() { }

    public static void showInCurrentWindow(Scene currentScene, String module, String startView)
            throws Exception {
        if (currentScene == null || !(currentScene.getWindow() instanceof Stage)) {
            throw new IllegalStateException("The Team Hub window is not available.");
        }

        Stage stage = (Stage) currentScene.getWindow();
        System.setProperty("loop.start", startView);

        switch (module) {
            case "customer":
                registerCustomerNavigation(currentScene, stage);
                new application.Main().start(stage);
                break;
            case "orders":
                new orders.App().start(stage);
                break;
            case "delivery":
                new main.App().start(stage);
                break;
            case "inventory":
                new LoopsFirstYearProject.LoopsFirstYearProject.App().start(stage);
                break;
            case "reviews":
                configureReviewCustomer();
                loop.reviews.Session.clearProductReturnNavigator();
                new loop.reviews.App().start(stage);
                break;
            case "finance":
                new gp.loop.App().start(stage);
                break;
            default:
                throw new IllegalArgumentException("Unknown module: " + module);
        }

        HubNavigation.install(stage);
    }

    /** Opens the selected food's review page and keeps a return path to its details. */
    public static void showProductReviews(Scene currentScene, FoodBarData food) throws Exception {
        if (currentScene == null || !(currentScene.getWindow() instanceof Stage)) {
            throw new IllegalStateException("The Product window is not available.");
        }
        if (food == null) {
            throw new IllegalArgumentException("A food must be selected before opening reviews.");
        }

        Stage stage = (Stage) currentScene.getWindow();
        configureReviewCustomer();
        System.setProperty("loop.start", "product-review");
        loop.reviews.Session.setSelectedProductId(food.getProductId());
        loop.reviews.Session.setProductReturnNavigator(
                () -> returnToFood(currentScene, stage, food));
        new loop.reviews.App().start(stage);
        HubNavigation.install(stage);
    }

    private static void registerCustomerNavigation(Scene scene, Stage stage) {
        SessionManager.setPersonalizedProductNavigator(() -> {
            try {
                ProductBrowseContext.usePersonalizedCatalogue();
                Parent root = App.loadFXML("Main Page");
                scene.setRoot(root);
                scene.getStylesheets().setAll(
                        App.class.getResource("styles.css").toExternalForm());
                stage.setTitle("Loop Products - Personalized");
                HubNavigation.install(stage);
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new IllegalStateException(
                        "Could not open the personalized Product catalogue.", exception);
            }
        });

        SessionManager.setReviewAdminNavigator(() -> {
            try {
                System.setProperty("loop.start", "admin");
                loop.reviews.Session.setAdminReturnNavigator(
                        () -> returnToCustomerAdmin(scene, stage));
                new loop.reviews.App().start(stage);
                HubNavigation.install(stage);
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new IllegalStateException(
                        "Could not open Review Moderation.", exception);
            }
        });
    }

    private static void configureReviewCustomer() {
        Customer customer = SessionManager.getCurrentCustomer();
        String name = customer == null ? "Guest" : customer.getName();
        String email = customer == null ? "guest@loop.local" : customer.getEmail();
        System.setProperty("loop.review.customer.name", name);
        System.setProperty("loop.review.customer.email", email);
    }

    private static void returnToFood(Scene scene, Stage stage, FoodBarData food) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("/ProductPage/ProductPage/View Food.fxml"));
            Parent root = loader.load();
            ViewFoodController controller = loader.getController();
            controller.setFood(food);
            scene.setRoot(root);
            scene.getStylesheets().setAll(
                    App.class.getResource("styles.css").toExternalForm());
            stage.setTitle("Loop Products - " + food.getProductName());
            HubNavigation.install(stage);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException(
                    "Could not return to the selected food.", exception);
        }
    }

    private static void returnToCustomerAdmin(Scene scene, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    application.Main.class.getResource("/view/SuperAdmin.fxml"));
            scene.setRoot(loader.load());
            scene.getStylesheets().setAll(
                    application.Main.class.getResource("/styles/app.css").toExternalForm());
            stage.setTitle("LOOP Customer Super Admin");
            HubNavigation.install(stage);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalStateException(
                    "Could not return to Customer Super Admin.", exception);
        }
    }
}
