package loop.reviews;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import loop.reviews.db.Database;
import loop.reviews.db.UserDao;
import loop.reviews.model.User;

/**
 * Application entry point for the LOOP Reviews and Ratings Component.
 *
 * Responsibilities:
 *  - Installs a global uncaught-exception handler on the JavaFX thread so
 *    nothing fails silently (STEP 3).
 *  - Initialises the SQLite database (creates tables + seeds sample data).
 *  - Boots the SceneManager on the primary Stage and opens the requested role view.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // STEP 3 - global uncaught exception handler on the JavaFX thread.
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("[UNCAUGHT EXCEPTION on " + thread.getName() + "]");
            throwable.printStackTrace();
            showFatal(throwable);
        });
        // Also cover any thread created after this point.
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("[UNCAUGHT EXCEPTION on " + thread.getName() + "]");
            throwable.printStackTrace();
        });

        try {
            // Create tables (IF NOT EXISTS), print DB path, seed sample data.
            Database.get().init();

            SceneManager.init(primaryStage);
            primaryStage.setTitle("LOOP  -  Reviews & Ratings");
            try {
                java.io.InputStream icon = App.class.getResourceAsStream("/images/appicon.png");
                if (icon != null) primaryStage.getIcons().add(new javafx.scene.image.Image(icon));
            } catch (Exception ignore) { /* icon is cosmetic - never fail startup */ }
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(680);
            String startView = System.getProperty("loop.start", "customer");
            UserDao users = new UserDao();
            boolean adminAccess = "admin".equalsIgnoreCase(startView) && Session.hasAdminGateway();
            User selected;
            if (adminAccess) {
                selected = users.findAll().stream()
                        .filter(user -> "ADMIN".equalsIgnoreCase(user.getRole()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No seeded review administrator found"));
            } else {
                String customerName = System.getProperty(
                        "loop.review.customer.name", "Tasmia Biswas");
                String customerEmail = System.getProperty(
                        "loop.review.customer.email", "tasmia@loop.com");
                selected = users.findOrCreateCustomer(customerName, customerEmail);
                if (!"guest@loop.local".equalsIgnoreCase(customerEmail)) {
                    Database.get().ensureCustomerReviewActivity(selected.getId());
                }
            }
            Session.setCurrentUser(selected);
            boolean selectedProduct = "product-review".equalsIgnoreCase(startView)
                    && Session.getSelectedProductId() > 0;
            SceneManager.switchTo(adminAccess
                    ? "admin_moderation"
                    : selectedProduct ? "product_reviews" : "home");
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("[FATAL] Failed to start application:");
            e.printStackTrace();
            showFatal(e);
        }
    }

    private void showFatal(Throwable t) {
        // Alerts must be shown on the FX thread.
        Runnable r = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected error");
            alert.setHeaderText("Something went wrong");
            alert.setContentText(String.valueOf(t.getMessage()));
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public static void main(String[] args) {
        if (System.getProperty("prism.order") == null) {
            System.setProperty("prism.order", "sw");
        }
        launch(args);
    }
}
