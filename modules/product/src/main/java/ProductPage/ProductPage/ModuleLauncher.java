package ProductPage.ProductPage;

import javafx.scene.Scene;
import javafx.stage.Stage;

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
}
