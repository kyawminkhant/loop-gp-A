package ProductPage.ProductPage;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/** Keeps a Back to Team Hub control available on every page inside a module. */
final class HubNavigation {

    private static final String BUTTON_ID = "loop-back-to-hub";
    private static final Map<Stage, HubNavigation> ACTIVE = new WeakHashMap<>();

    private final Stage stage;
    private final ChangeListener<Scene> sceneListener;
    private ChangeListener<Parent> rootListener;
    private Scene observedScene;

    private HubNavigation(Stage stage) {
        this.stage = stage;
        this.sceneListener = (observable, oldScene, newScene) -> observe(newScene);
    }

    static void install(Stage stage) {
        uninstall(stage);
        HubNavigation navigation = new HubNavigation(stage);
        ACTIVE.put(stage, navigation);
        stage.sceneProperty().addListener(navigation.sceneListener);
        navigation.observe(stage.getScene());
    }

    static void uninstall(Stage stage) {
        HubNavigation existing = ACTIVE.remove(stage);
        if (existing != null) {
            existing.detach();
        }
    }

    private void observe(Scene scene) {
        detachRootListener();
        observedScene = scene;
        if (scene == null) {
            return;
        }

        rootListener = (observable, oldRoot, newRoot) -> addBackButton(newRoot);
        scene.rootProperty().addListener(rootListener);
        addBackButton(scene.getRoot());
    }

    private void addBackButton(Parent root) {
        if (!(root instanceof Pane) || root.lookup("#" + BUTTON_ID) != null) {
            return;
        }

        Button backButton = new Button("\u2190  Back to Team Hub");
        backButton.setId(BUTTON_ID);
        backButton.setManaged(false);
        backButton.setFocusTraversable(false);
        backButton.setLayoutX(18);
        backButton.setLayoutY(18);
        backButton.setStyle(
                "-fx-background-color: #68151f;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 10 18 10 18;"
                + "-fx-background-radius: 22;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 10, 0.25, 0, 3);");
        backButton.setOnAction(event -> returnToHub());

        ((Pane) root).getChildren().add(backButton);
        backButton.toFront();
    }

    private void returnToHub() {
        uninstall(stage);
        try {
            App.showHub(stage);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void detach() {
        stage.sceneProperty().removeListener(sceneListener);
        detachRootListener();
    }

    private void detachRootListener() {
        if (observedScene != null && rootListener != null) {
            observedScene.rootProperty().removeListener(rootListener);
        }
        rootListener = null;
        observedScene = null;
    }
}
